/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.facade.providers;

import org.apache.commons.lang.StringUtils;
import org.jboss.pnc.bpm.BpmEventType;
import org.jboss.pnc.bpm.BpmManager;
import org.jboss.pnc.bpm.task.SCMRepositoryCreationTask;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.ScmModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.common.util.UrlUtils;
import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.dto.SCMRepository;
import org.jboss.pnc.dto.internal.bpm.RepositoryCreationProcess;
import org.jboss.pnc.dto.requests.CreateAndSyncSCMRequest;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.TaskResponse;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.facade.mapper.api.SCMRepositoryMapper;
import org.jboss.pnc.facade.providers.api.SCMRepositoryProvider;
import org.jboss.pnc.facade.util.UserService;
import org.jboss.pnc.facade.validation.ConflictedEntryException;
import org.jboss.pnc.facade.validation.InvalidEntityException;
import org.jboss.pnc.facade.validation.ValidationBuilder;
import org.jboss.pnc.model.RepositoryConfiguration;
import org.jboss.pnc.rest.restmodel.bpm.BpmNotificationRest;
import org.jboss.pnc.spi.datastore.repositories.RepositoryConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.pnc.spi.notifications.Notifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static org.jboss.pnc.spi.datastore.predicates.RepositoryConfigurationPredicates.matchByScmUrl;
import static org.jboss.pnc.spi.datastore.predicates.RepositoryConfigurationPredicates.searchByScmUrl;

@Stateless
public class SCMRepositoryProviderImpl
        extends AbstractProvider<RepositoryConfiguration, SCMRepository, SCMRepository> implements SCMRepositoryProvider {

    private static final Logger log = LoggerFactory.getLogger(SCMRepositoryProviderImpl.class);
    private ScmModuleConfig config;
    private RepositoryConfigurationRepository repositoryConfigurationRepository;
    private BpmManager bpmManager;
    private Notifier wsNotifier;
    private UserService userService;
    //FIXME do we have something else, then this regex??
    private static final Pattern REPOSITORY_NAME_PATTERN = Pattern.compile("(\\/[\\w\\.:\\~_-]+)+(\\.git)(?:\\/?|\\#[\\d\\w\\.\\-_]+?)$");


    @Inject
    public SCMRepositoryProviderImpl(RepositoryConfigurationRepository repository,
            SCMRepositoryMapper mapper,
            Configuration configuration,
            BpmManager bpmManager,
            Notifier wsNotifier,
            UserService userService) throws ConfigurationParseException {
        super(repository, mapper, RepositoryConfiguration.class);
        this.repositoryConfigurationRepository = repository;
        this.config = configuration.getModuleConfig(new PncConfigProvider<>(ScmModuleConfig.class));
        this.bpmManager = bpmManager;
        this.wsNotifier = wsNotifier;
        this.userService = userService;
    }

    public Page<SCMRepository> getAllWithMatchAndSearchUrl(int pageIndex,
                                                           int pageSize,
                                                           String sortingRsql,
                                                           String query,
                                                           String matchUrl,
                                                           String searchUrl) {

        List<Predicate<RepositoryConfiguration>> predicates = new ArrayList<>();

        addToListIfStringNotNullAndNotEmpty(predicates, matchUrl, matchByScmUrl(matchUrl));
        addToListIfStringNotNullAndNotEmpty(predicates, searchUrl, searchByScmUrl(searchUrl));

        // transform list to array for 'predicates' varargs in 'queryForCollection' method
        Predicate<RepositoryConfiguration>[] predicatesArray = new Predicate[predicates.size()];
        predicatesArray = predicates.toArray(predicatesArray);

        return queryForCollection(pageIndex, pageSize, sortingRsql, query, predicatesArray);
    }

    /**
     * Add item to the list if the string is not null *and* not empty
     *
     * @param list
     * @param str
     * @param item
     * @param <T>
     */
    private <T> void addToListIfStringNotNullAndNotEmpty(List<T> list, String str, T item) {

        if (str != null && !str.isEmpty()) {
            list.add(item);
        }
    }

    public TaskResponse createSCMRepositoryWithOneUrl(CreateAndSyncSCMRequest scmRequest,
            BuildConfiguration configuration,
            Consumer<BpmNotificationRest> onSuccessConsumer) throws CoreException {
        log.debug("Received request to start RC creation with url autodetect: " + scmRequest);

        ValidationBuilder.validateObject(scmRequest, WhenCreatingNew.class).validateAnnotations();
        if (configuration != null)
            ValidationBuilder.validateObject(configuration, WhenCreatingNew.class).validateAnnotations();


        SCMRepository.Builder scmBuilder = SCMRepository.builder();

        String internalScmAuthority = config.getInternalScmAuthority();
        String scmUrl = scmRequest.getScmUrl();
        Boolean isUrlInternal = scmUrl.contains(internalScmAuthority);
        if (isUrlInternal) {
            validateInternalRepository(scmUrl);
            checkIfInternalUrlExists(scmUrl);
            if (scmRequest.getPreBuildSyncEnabled() != null && scmRequest.getPreBuildSyncEnabled()) {
                throw new InvalidEntityException("Pre-build sync cannot be enabled without external repository url.");
            }
            scmBuilder.internalUrl(scmUrl);
        } else {
            checkIfExternalUrlExists(scmUrl);
            if (scmRequest.getPreBuildSyncEnabled() == null) {
                scmBuilder.preBuildSyncEnabled(true);
            } else {
                scmBuilder.preBuildSyncEnabled(scmRequest.getPreBuildSyncEnabled());
            }
            scmBuilder.externalUrl(scmUrl);
        }
        SCMRepository scmRepository = scmBuilder.build();
        SCMRepositoryCreationTask task = startRCreationTask(scmRepository,configuration, onSuccessConsumer);

        return TaskResponse.builder().taskId(task.getTaskId()).build();
    }

    private SCMRepositoryCreationTask startRCreationTask(SCMRepository scmRepository,
            BuildConfiguration buildConfiguration,
            Consumer<BpmNotificationRest> onSuccessConsumer) throws CoreException {
        //FIXME SCMRepoCreationTask, BpmManager, BpmNotificationRest and BpmEventTypes
        // are directly or transitively dependant on rest-model

        String userToken = userService.currentUserToken();
        String revision = null;
        if (buildConfiguration != null) {
            revision = buildConfiguration.getScmRevision();
        }

        RepositoryCreationProcess repositoryCreationProcess = RepositoryCreationProcess.builder()
                .scmRepository(scmRepository)
                .revision(revision)
                .build();

        SCMRepositoryCreationTask repositoryCreationTask = new SCMRepositoryCreationTask(
                repositoryCreationProcess, userToken);

        if (onSuccessConsumer != null) {
            repositoryCreationTask.addListener(BpmEventType.RC_CREATION_SUCCESS,
                    onSuccessConsumer);
        } else {
            repositoryCreationTask.addListener(BpmEventType.RC_CREATION_SUCCESS,
                    notification -> wsNotifier.sendMessage(notification));
        }

        repositoryCreationTask.addListener(BpmEventType.RC_CREATION_ERROR,
                x -> log.debug("Received BPM event RC_CREATION_ERROR: " + x));

        addWebsocketForwardingListeners(repositoryCreationTask);

        try {
            bpmManager.startTask(repositoryCreationTask);
        } catch (CoreException e) {
            throw new CoreException("Could not start BPM task: " + repositoryCreationTask, e);
        }
        return repositoryCreationTask;
    }

    public void validateInternalRepository(String internalRepoUrl) throws InvalidEntityException {
        String internalScmAuthority = config.getInternalScmAuthority();
        if (!isInternalRepository(internalScmAuthority, internalRepoUrl)) {
            log.warn("Invalid internal repo url: " + internalRepoUrl);
            throw new InvalidEntityException("Internal repository url has to start with: <protocol>://" + internalScmAuthority +
                    " followed by a repository name or match the pattern: " + REPOSITORY_NAME_PATTERN);
        }

    }

    public static Boolean isInternalRepository(String internalScmAuthority, String internalRepoUrl) {
        if (StringUtils.isBlank(internalRepoUrl) || internalScmAuthority == null) {
            throw new RuntimeException("InternalScmAuthority and internalRepoUrl parameters must be set.");
        }
        String internalRepoUrlNoProto = UrlUtils.stripProtocol(internalRepoUrl);
        return internalRepoUrlNoProto.startsWith(internalScmAuthority)
                && REPOSITORY_NAME_PATTERN.matcher(internalRepoUrlNoProto.replace(internalScmAuthority, "")).matches();
    }

    private void checkIfInternalUrlExists(String internalUrl) throws ConflictedEntryException {
        if (internalUrl != null) {
            RepositoryConfiguration repositoryConfiguration = repositoryConfigurationRepository.queryByInternalScm(internalUrl);
            if (repositoryConfiguration != null) {
                String message = "{ \"repositoryConfigurationId\" : " + repositoryConfiguration.getId() + "}";
                throw new ConflictedEntryException(message,RepositoryConfiguration.class, repositoryConfiguration.getId());
            }
        }
    }

    private void checkIfExternalUrlExists(String externalUrl) throws ConflictedEntryException {
        if (externalUrl != null) {
            RepositoryConfiguration repositoryConfiguration = repositoryConfigurationRepository.queryByExternalScm(externalUrl);
            if (repositoryConfiguration != null) {
                String message = "{ \"repositoryConfigurationId\" : " + repositoryConfiguration.getId() + "}";
                throw new ConflictedEntryException(message,RepositoryConfiguration.class, repositoryConfiguration.getId());
            }
        }
    }
    private void addWebsocketForwardingListeners(SCMRepositoryCreationTask task) {
        Consumer<? extends BpmNotificationRest> doNotify = (e) -> wsNotifier.sendMessage(e);
        task.addListener(BpmEventType.RC_REPO_CREATION_SUCCESS, doNotify);
        task.addListener(BpmEventType.RC_REPO_CREATION_ERROR, doNotify);
        task.addListener(BpmEventType.RC_REPO_CLONE_SUCCESS, doNotify);
        task.addListener(BpmEventType.RC_REPO_CLONE_ERROR, doNotify);
        //clients are notified from callback in startRCreationTask
        //task.addListener(BpmEventType.RC_CREATION_SUCCESS, doNotify);
        task.addListener(BpmEventType.RC_CREATION_ERROR, doNotify);
    }

}
