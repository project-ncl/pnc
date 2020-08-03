/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.bpm.BpmEventType;
import org.jboss.pnc.bpm.BpmManager;
import org.jboss.pnc.bpm.BpmTask;
import org.jboss.pnc.bpm.model.BpmStringMapNotificationRest;
import org.jboss.pnc.bpm.model.RepositoryCreationProcess;
import org.jboss.pnc.bpm.model.RepositoryCloneSuccess;
import org.jboss.pnc.bpm.task.RepositoryCreationTask;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.concurrent.MDCWrappers;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.ScmModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.common.util.UrlUtils;
import org.jboss.pnc.dto.SCMRepository;
import org.jboss.pnc.dto.notification.RepositoryCreationFailure;
import org.jboss.pnc.dto.notification.SCMRepositoryCreationSuccess;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.RepositoryCreationResponse;
import org.jboss.pnc.enums.JobNotificationType;
import org.jboss.pnc.facade.providers.api.SCMRepositoryProvider;
import org.jboss.pnc.facade.util.RepourClient;
import org.jboss.pnc.facade.util.UserService;
import org.jboss.pnc.facade.validation.ConflictedEntryException;
import org.jboss.pnc.facade.validation.InvalidEntityException;
import org.jboss.pnc.mapper.api.SCMRepositoryMapper;
import org.jboss.pnc.model.RepositoryConfiguration;
import org.jboss.pnc.spi.datastore.repositories.RepositoryConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.pnc.spi.notifications.Notifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static org.jboss.pnc.constants.Patterns.INTERNAL_REPOSITORY_NAME;
import static org.jboss.pnc.enums.JobNotificationType.SCM_REPOSITORY_CREATION;
import static org.jboss.pnc.spi.datastore.predicates.RepositoryConfigurationPredicates.matchByScmUrl;
import static org.jboss.pnc.spi.datastore.predicates.RepositoryConfigurationPredicates.searchByScmUrl;
import static org.jboss.pnc.spi.datastore.predicates.RepositoryConfigurationPredicates.withExactInternalScmRepoUrl;

@PermitAll
@Stateless
public class SCMRepositoryProviderImpl
        extends AbstractProvider<Integer, RepositoryConfiguration, SCMRepository, SCMRepository>
        implements SCMRepositoryProvider {

    private static final Logger log = LoggerFactory.getLogger(SCMRepositoryProviderImpl.class);

    private static final String RC_REPO_CREATION_CONFLICT = "RC_REPO_CREATION_CONFLICT";

    private ScmModuleConfig config;

    private static final Pattern REPOSITORY_NAME_PATTERN = Pattern.compile(INTERNAL_REPOSITORY_NAME);

    @Inject
    private RepositoryConfigurationRepository repositoryConfigurationRepository;

    @Inject
    private UserService userService;

    @Inject
    private Notifier notifier;

    @Inject
    private BpmManager bpmManager;

    @Inject
    private RepourClient repour;

    @Inject
    public SCMRepositoryProviderImpl(
            RepositoryConfigurationRepository repository,
            SCMRepositoryMapper mapper,
            Configuration configuration) throws ConfigurationParseException {
        super(repository, mapper, RepositoryConfiguration.class);
        this.config = configuration.getModuleConfig(new PncConfigProvider<>(ScmModuleConfig.class));
    }

    @Override
    public void delete(String id) {
        throw new UnsupportedOperationException("Deleting scm repositories is prohibited!");
    }

    @Override
    public Page<SCMRepository> getAllWithMatchAndSearchUrl(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            String matchUrl,
            String searchUrl) {

        List<Predicate<RepositoryConfiguration>> predicates = new ArrayList<>();

        addToListIfStringNotNullAndNotEmpty(predicates, matchUrl, () -> matchByScmUrl(matchUrl));
        addToListIfStringNotNullAndNotEmpty(predicates, searchUrl, () -> searchByScmUrl(searchUrl));

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
    private <T> void addToListIfStringNotNullAndNotEmpty(List<T> list, String str, Supplier<T> item) {
        if (str != null && !str.isEmpty()) {
            list.add(item.get());
        }
    }

    @Override
    public RepositoryCreationResponse createSCMRepository(String scmUrl, Boolean preBuildSyncEnabled) {
        return createSCMRepository(scmUrl, preBuildSyncEnabled, SCM_REPOSITORY_CREATION, this::onSCMRepositoryCreated);
    }

    private void onSCMRepositoryCreated(RepositoryCreated event) {
        final SCMRepository repository = getSpecific(Integer.toString(event.getRepositoryId()));
        final String taskId = event.getTaskId() == null ? null : event.getTaskId().toString();
        if (taskId != null)
            notifier.sendMessage(new SCMRepositoryCreationSuccess(repository, taskId));
    }

    @Override
    public RepositoryCreationResponse createSCMRepository(
            String scmUrl,
            Boolean preBuildSyncEnabled,
            JobNotificationType jobType,
            Consumer<RepositoryCreated> consumer) {
        log.trace(
                "Received request to start RC creation with url autodetect: " + scmUrl + " (sync enabled? "
                        + preBuildSyncEnabled + ")");
        if (StringUtils.isEmpty(scmUrl))
            throw new InvalidEntityException("You must specify the SCM URL.");

        if (scmUrl.contains(config.getInternalScmAuthority())) {

            // validation phase
            validateInternalRepository(scmUrl);
            validateRepositoryWithInternalURLDoesNotExist(scmUrl);

            SCMRepository scmRepository = createSCMRepositoryFromValues(null, scmUrl, false);

            consumer.accept(new RepositoryCreated(null, Integer.valueOf(scmRepository.getId())));
            return new RepositoryCreationResponse(scmRepository);

        } else {
            validateRepositoryWithExternalURLDoesNotExist(scmUrl);

            boolean sync = preBuildSyncEnabled == null || preBuildSyncEnabled;
            BpmTask task = startRCreationTask(scmUrl, sync, jobType, consumer);

            return new RepositoryCreationResponse(task.getTaskId());
        }
    }

    @Override
    public SCMRepository update(String id, SCMRepository restEntity) {
        validateBeforeUpdating(id, restEntity);
        SCMRepository before = getSpecific(id);
        if (!before.getInternalUrl().equals(restEntity.getInternalUrl())) {
            throw new InvalidEntityException("Updating internal URL is prohibited. SCMRepo: " + id);
        }
        log.debug("Updating entity: " + restEntity.toString());
        RepositoryConfiguration saved = repository.save(mapper.toEntity(restEntity));
        return mapper.toDTO(saved);
    }

    private SCMRepository createSCMRepositoryFromValues(
            String externalScmUrl,
            String internalScmUrl,
            boolean preBuildSyncEnabled) {

        RepositoryConfiguration.Builder built = RepositoryConfiguration.Builder.newBuilder()
                .internalUrl(internalScmUrl)
                .preBuildSyncEnabled(preBuildSyncEnabled);

        if (externalScmUrl != null) {
            built.externalUrl(externalScmUrl);
        }

        RepositoryConfiguration entity = repository.save(built.build());
        return mapper.toDTO(entity);
    }

    public void validateInternalRepository(String internalRepoUrl) throws InvalidEntityException {
        String internalScmAuthority = config.getInternalScmAuthority();
        if (!isInternalRepository(internalScmAuthority, internalRepoUrl)) {
            log.info("Invalid internal repo url: " + internalRepoUrl);
            throw new InvalidEntityException(
                    "Internal repository url has to start with: <protocol>://" + internalScmAuthority
                            + " followed by a repository name or match the pattern: " + REPOSITORY_NAME_PATTERN);
        } else if (internalRepoUrl.contains("/gerrit/")) {
            log.info("Invalid internal repo url: " + internalRepoUrl);
            throw new InvalidEntityException(
                    "Incorrect format of internal repository. Internal repository"
                            + " url should not contain '/gerrit/' part of the url");
        }
    }

    public static Boolean isInternalRepository(String internalScmAuthority, String internalRepoUrl) {
        if (StringUtils.isEmpty(internalRepoUrl) || internalScmAuthority == null) {
            throw new IllegalArgumentException("InternalScmAuthority and internalRepoUrl parameters must be set.");
        }
        String internalRepoUrlNoProto = UrlUtils.stripProtocol(internalRepoUrl);
        String internalRepoName = internalRepoUrlNoProto.replace(internalScmAuthority, "");
        return internalRepoUrlNoProto.startsWith(internalScmAuthority)
                && REPOSITORY_NAME_PATTERN.matcher(internalRepoName).matches();
    }

    private void validateRepositoryWithInternalURLDoesNotExist(String internalUrl) throws ConflictedEntryException {
        RepositoryConfiguration repositoryConfiguration = repositoryConfigurationRepository
                .queryByInternalScm(internalUrl);
        if (repositoryConfiguration != null) {
            String message = "SCM Repository already exists (id: " + repositoryConfiguration.getId() + ")";
            throw new ConflictedEntryException(
                    message,
                    RepositoryConfiguration.class,
                    repositoryConfiguration.getId().toString());
        }
    }

    private void validateRepositoryWithExternalURLDoesNotExist(String externalUrl) throws ConflictedEntryException {
        RepositoryConfiguration repositoryConfiguration = repositoryConfigurationRepository
                .queryByExternalScm(externalUrl);
        if (repositoryConfiguration != null) {
            String message = "SCM Repository already exists (id: " + repositoryConfiguration.getId() + ")";
            throw new ConflictedEntryException(
                    message,
                    RepositoryConfiguration.class,
                    repositoryConfiguration.getId().toString());
        }
        String internalUrl = repour.translateExternalUrl(externalUrl);
        validateRepositoryWithInternalURLDoesNotExist(internalUrl);
    }

    private RepositoryCreationTask startRCreationTask(
            String externalURL,
            boolean preBuildSyncEnabled,
            JobNotificationType jobType,
            Consumer<RepositoryCreated> consumer) {
        String userToken = userService.currentUserToken();

        org.jboss.pnc.bpm.model.RepositoryConfiguration repositoryConfiguration = org.jboss.pnc.bpm.model.RepositoryConfiguration
                .builder()
                .externalUrl(externalURL)
                .preBuildSyncEnabled(preBuildSyncEnabled)
                .build();

        RepositoryCreationProcess repositoryCreationProcess = RepositoryCreationProcess.builder()
                .repositoryConfiguration(repositoryConfiguration)
                .build();

        RepositoryCreationTask repositoryCreationTask = new RepositoryCreationTask(
                repositoryCreationProcess,
                userToken);

        Consumer<RepositoryCloneSuccess> successListener = event -> onRepoCloneSuccess(
                event,
                repositoryCreationTask,
                consumer,
                jobType,
                externalURL,
                preBuildSyncEnabled);
        repositoryCreationTask.addListener(BpmEventType.RC_REPO_CLONE_SUCCESS, MDCWrappers.wrap(successListener));
        addErrorListeners(jobType, repositoryCreationTask);

        try {
            bpmManager.startTask(repositoryCreationTask);
        } catch (CoreException e) {
            throw new RuntimeException("Could not start BPM task: " + repositoryCreationTask, e);
        }
        return repositoryCreationTask;
    }

    private void onRepoCloneSuccess(
            RepositoryCloneSuccess event,
            RepositoryCreationTask repositoryCreationTask,
            Consumer<RepositoryCreated> consumer,
            JobNotificationType jobType,
            String externalURL,
            boolean preBuildSyncEnabled) throws NumberFormatException {
        final String internalScmUrl = event.getData().getInternalUrl();
        final Integer taskId = repositoryCreationTask.getTaskId();

        RepositoryConfiguration existing = repository.queryByPredicates(withExactInternalScmRepoUrl(internalScmUrl));
        if (existing != null) {
            RepositoryCreationFailure error = new RepositoryCreationFailure(
                    jobType,
                    RC_REPO_CREATION_CONFLICT,
                    existing,
                    taskId.toString());
            notifier.sendMessage(error);
        } else {
            SCMRepository scmRepo = createSCMRepositoryFromValues(externalURL, internalScmUrl, preBuildSyncEnabled);
            RepositoryCreated notification = new RepositoryCreated(taskId, Integer.parseInt(scmRepo.getId()));
            consumer.accept(notification);
        }
    }

    private void addErrorListeners(JobNotificationType jobType, RepositoryCreationTask task) {
        Consumer<BpmStringMapNotificationRest> doNotifySMNError = MDCWrappers
                .wrap((e) -> notifier.sendMessage(mapError(jobType, e, task)));
        task.addListener(BpmEventType.RC_REPO_CREATION_ERROR, doNotifySMNError);
        task.addListener(BpmEventType.RC_REPO_CLONE_ERROR, doNotifySMNError);
    }

    private RepositoryCreationFailure mapError(
            JobNotificationType jobType,
            BpmStringMapNotificationRest notification,
            BpmTask task) {
        log.debug("Received BPM event error: " + notification);
        final String taskId = task.getTaskId() == null ? null : task.getTaskId().toString();
        return new RepositoryCreationFailure(jobType, notification.getEventType(), notification.getData(), taskId);
    }
}
