/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.auth.KeycloakServiceClient;
import org.jboss.pnc.bpm.BpmEventType;
import org.jboss.pnc.bpm.Connector;
import org.jboss.pnc.bpm.model.RepositoryCreationProcess;
import org.jboss.pnc.bpm.task.RepositoryCreationTask;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.concurrent.Sequence;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.json.moduleconfig.BpmModuleConfig;
import org.jboss.pnc.common.json.moduleconfig.ScmModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.common.util.UrlUtils;
import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.dto.SCMRepository;
import org.jboss.pnc.dto.notification.RepositoryCreationFailure;
import org.jboss.pnc.dto.notification.SCMRepositoryCreationSuccess;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.RepositoryCreationResponse;
import org.jboss.pnc.dto.tasks.RepositoryCreationResult;
import org.jboss.pnc.enums.JobNotificationType;
import org.jboss.pnc.facade.providers.api.BuildConfigurationProvider;
import org.jboss.pnc.facade.providers.api.SCMRepositoryProvider;
import org.jboss.pnc.facade.util.RepourClient;
import org.jboss.pnc.facade.validation.ConflictedEntryException;
import org.jboss.pnc.facade.validation.InvalidEntityException;
import org.jboss.pnc.mapper.api.SCMRepositoryMapper;
import org.jboss.pnc.model.RepositoryConfiguration;
import org.jboss.pnc.spi.datastore.predicates.RepositoryConfigurationPredicates;
import org.jboss.pnc.spi.datastore.repositories.RepositoryConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.pnc.spi.exception.ProcessManagerException;
import org.jboss.pnc.spi.notifications.Notifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

import static org.jboss.pnc.constants.Patterns.INTERNAL_REPOSITORY_NAME;
import static org.jboss.pnc.enums.JobNotificationType.SCM_REPOSITORY_CREATION;
import static org.jboss.pnc.spi.datastore.predicates.RepositoryConfigurationPredicates.withExactInternalScmRepoUrl;

@PermitAll
@Stateless
public class SCMRepositoryProviderImpl
        extends AbstractUpdatableProvider<Integer, RepositoryConfiguration, SCMRepository, SCMRepository>
        implements SCMRepositoryProvider {

    private static final Logger log = LoggerFactory.getLogger(SCMRepositoryProviderImpl.class);

    private static final String RC_REPO_CREATION_CONFLICT = "RC_REPO_CREATION_CONFLICT";

    private ScmModuleConfig config;

    private static final Pattern REPOSITORY_NAME_PATTERN = Pattern.compile(INTERNAL_REPOSITORY_NAME);

    @Inject
    private RepositoryConfigurationRepository repositoryConfigurationRepository;

    @Inject
    private KeycloakServiceClient keycloakServiceClient;

    @Inject
    private Notifier notifier;

    @Inject
    private BpmModuleConfig bpmConfig;

    @Inject
    private GlobalModuleGroup globalConfig;

    @Inject
    private RepourClient repour;

    @Inject
    private BuildConfigurationProvider buildConfigurationProvider;

    @Inject
    Connector connector;

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

        validateAndAddPredicate(predicates, matchUrl, RepositoryConfigurationPredicates::matchByScmUrl);
        validateAndAddPredicate(predicates, searchUrl, RepositoryConfigurationPredicates::searchByScmUrl);

        // transform list to array for 'predicates' varargs in 'queryForCollection' method
        Predicate<RepositoryConfiguration>[] predicatesArray = new Predicate[predicates.size()];
        predicatesArray = predicates.toArray(predicatesArray);

        return queryForCollection(pageIndex, pageSize, sortingRsql, query, predicatesArray);
    }

    /**
     * Creates predicate and add it to the list if the string is not null *and* not empty.
     *
     * @param list
     * @param str
     * @param item
     * @param <T>
     * @throws InvalidEntityException when IllegalArgumentException is thrown when creating the predicate.
     */
    private <T> void validateAndAddPredicate(List<Predicate<T>> list, String str, Function<String, Predicate<T>> item) {
        if (str != null && !str.isEmpty()) {
            try {
                list.add(item.apply(str));
            } catch (IllegalArgumentException ex) {
                throw new InvalidEntityException(ex.getMessage());
            }
        }
    }

    @Override
    public RepositoryCreationResponse createSCMRepository(String scmUrl, Boolean preBuildSyncEnabled) {
        RepositoryCreationResponse scmRepository = createSCMRepository(
                scmUrl,
                preBuildSyncEnabled,
                SCM_REPOSITORY_CREATION,
                Optional.empty());
        // if it is an internal only repository, it should be created at this point and notification is sent now,
        // otherwise the notification is sent on when the callback of cloning is processed
        if (scmRepository.getTaskId() == null) {
            notifySCMRepositoryCreated(
                    new RepositoryCreated(null, Integer.valueOf(scmRepository.getRepository().getId())));
        }
        return scmRepository;
    }

    private void notifySCMRepositoryCreated(RepositoryCreated event) {
        final SCMRepository repository = getSpecific(Integer.toString(event.getRepositoryId()));
        final String taskId = event.getTaskId() == null ? null : event.getTaskId().toString();
        if (taskId != null)
            notifier.sendMessage(new SCMRepositoryCreationSuccess(repository, taskId));
    }

    /**
     * Starts the task of creating SCMRepository.If the SCM URL is external, the task creates new internal repository
     * and does initial synchronization.
     *
     * @param scmUrl The URL of the SCM repository.
     * @param preBuildSyncEnabled If the SCM URL is external, this parameter specifies whether the external repository
     *        should be synchronized into the internal one before build.
     * @param jobType Type of the job that requested the SCM repository creation (for notification purposes).
     * @return id of the created
     */
    public RepositoryCreationResponse createSCMRepository(
            String scmUrl,
            Boolean preBuildSyncEnabled,
            JobNotificationType jobType,
            Optional<BuildConfiguration> buildConfiguration) {
        return createSCMRepository(scmUrl, null, preBuildSyncEnabled, jobType, buildConfiguration);
    }

    @Override
    public RepositoryCreationResponse createSCMRepository(
            String scmUrl,
            String revision,
            Boolean preBuildSyncEnabled,
            JobNotificationType jobType,
            Optional<BuildConfiguration> buildConfiguration) {
        log.trace(
                "Received request to start RC creation with url autodetect: " + scmUrl + " (sync enabled? "
                        + preBuildSyncEnabled + ")");
        if (StringUtils.isEmpty(scmUrl))
            throw new InvalidEntityException("You must specify the SCM URL.");

        if (scmUrl.contains(config.getInternalScmAuthority())) { // is internal repository
            // validation phase
            validateInternalRepository(scmUrl);
            validateRepositoryWithInternalURLDoesNotExist(scmUrl, null);

            SCMRepository scmRepository = createSCMRepositoryFromValues(null, scmUrl, false);
            return new RepositoryCreationResponse(scmRepository);
        } else {
            // External repository needs to be cloned first. Starting the process ...
            validateRepositoryWithExternalURLDoesNotExist(scmUrl, null);

            boolean sync = preBuildSyncEnabled == null || preBuildSyncEnabled;
            Long taskId = startRCreationTask(scmUrl, revision, sync, jobType, buildConfiguration);

            return new RepositoryCreationResponse(taskId);
        }
    }

    @Override
    public void validateBeforeUpdating(Integer id, SCMRepository restEntity) {
        super.validateBeforeUpdating(id, restEntity);
        RepositoryConfiguration entityInDb = findInDB(id);
        if (!entityInDb.getInternalUrl().equals(restEntity.getInternalUrl())) {
            throw new InvalidEntityException("Updating internal URL is prohibited. SCMRepo: " + id);
        }
        if (restEntity.getExternalUrl() != null && !restEntity.getExternalUrl().equals(entityInDb.getExternalUrl())) {
            validateRepositoryWithExternalURLDoesNotExist(restEntity.getExternalUrl(), id);
        }
    }

    /**
     * Crates and store SCMRepository to DB.
     */
    private SCMRepository createSCMRepositoryFromValues(
            String externalScmUrl,
            String internalScmUrl,
            boolean preBuildSyncEnabled) {

        RepositoryConfiguration.Builder rcBuilder = RepositoryConfiguration.Builder.newBuilder()
                .internalUrl(internalScmUrl)
                .preBuildSyncEnabled(preBuildSyncEnabled);

        if (externalScmUrl != null) {
            rcBuilder.externalUrl(externalScmUrl);
        }

        RepositoryConfiguration entity = repository.save(rcBuilder.build());
        log.info("Created SCM repository: {}.", entity.toString());
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
        } else if (internalRepoUrl.split("://")[0].contains("http")) {
            log.info("Invalid internal repo url: " + internalRepoUrl);
            throw new InvalidEntityException(
                    "Incorrect url protocol. Http and https protocols are not supported for internal repository. Try using 'git+ssh'");
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

    private void validateRepositoryWithInternalURLDoesNotExist(String internalUrl, Integer ignoreId)
            throws ConflictedEntryException {
        RepositoryConfiguration repositoryConfiguration = repositoryConfigurationRepository
                .queryByInternalScm(internalUrl);
        if (repositoryConfiguration != null && !repositoryConfiguration.getId().equals(ignoreId)) {
            String message = "SCM Repository with internal URL '" + internalUrl + "'already exists (id: "
                    + repositoryConfiguration.getId() + ")";
            throw new ConflictedEntryException(
                    message,
                    RepositoryConfiguration.class,
                    repositoryConfiguration.getId().toString());
        }
    }

    private void validateRepositoryWithExternalURLDoesNotExist(String externalUrl, Integer ignoreId)
            throws ConflictedEntryException {
        RepositoryConfiguration repositoryConfiguration = repositoryConfigurationRepository
                .queryByExternalScm(externalUrl);
        if (repositoryConfiguration != null && !repositoryConfiguration.getId().equals(ignoreId)) {
            String message = "SCM Repository with external URL '" + externalUrl + "' already exists (id: "
                    + repositoryConfiguration.getId() + ")";
            throw new ConflictedEntryException(
                    message,
                    RepositoryConfiguration.class,
                    repositoryConfiguration.getId().toString());
        }
        String internalUrl = repour.translateExternalUrl(externalUrl);
        validateRepositoryWithInternalURLDoesNotExist(internalUrl, ignoreId);
    }

    /**
     *
     * @param externalURL
     * @param preBuildSyncEnabled
     * @param jobType
     * @param buildConfiguration required when repository is created as part of BC creation process
     * @return
     */
    private Long startRCreationTask(
            String externalURL,
            String revision,
            boolean preBuildSyncEnabled,
            JobNotificationType jobType,
            Optional<BuildConfiguration> buildConfiguration) {
        String authToken = keycloakServiceClient.getAuthToken();

        org.jboss.pnc.bpm.model.RepositoryConfiguration repositoryConfiguration = org.jboss.pnc.bpm.model.RepositoryConfiguration
                .builder()
                .externalUrl(externalURL)
                .preBuildSyncEnabled(preBuildSyncEnabled)
                .build();

        RepositoryCreationTask task;
        RepositoryCreationProcess.RepositoryCreationProcessBuilder repositoryCreationProcess = RepositoryCreationProcess
                .builder()
                .repositoryConfiguration(repositoryConfiguration)
                .revision(revision);

        buildConfiguration.ifPresent(repositoryCreationProcess::buildConfiguration);
        task = new RepositoryCreationTask(repositoryCreationProcess.build(), jobType, globalConfig);

        long id = Sequence.nextId();
        try {
            Map<String, Serializable> parameters = new HashMap<>();
            parameters.put("processParameters", task.getProcessParameters());
            parameters.put("taskId", id);
            connector.startProcess(bpmConfig.getNewBcCreationProcessId(), parameters, Objects.toString(id), authToken);
        } catch (CoreException e) {
            throw new RuntimeException("Could not get process parameters: " + task, e);
        } catch (ProcessManagerException e) {
            throw new RuntimeException("Could not start BPM task using REST connector: " + task, e);
        }
        return id;
    }

    @Override
    public void repositoryCreationCompleted(RepositoryCreationResult result) {
        if (result.getStatus().isSuccess()) {
            RepositoryConfiguration existing = repository
                    .queryByPredicates(withExactInternalScmRepoUrl(result.getInternalScmUrl()));
            if (existing != null) {
                RepositoryCreationFailure error = new RepositoryCreationFailure(
                        result.getJobType(),
                        RC_REPO_CREATION_CONFLICT,
                        existing,
                        result.getTaskId().toString());
                notifier.sendMessage(error);
            } else {
                SCMRepository scmRepo = createSCMRepositoryFromValues(
                        result.getExternalUrl(),
                        result.getInternalScmUrl(),
                        result.isPreBuildSyncEnabled());
                RepositoryCreated notification = new RepositoryCreated(
                        result.getTaskId(),
                        Integer.parseInt(scmRepo.getId()));
                log.debug("Repository created: {}", notification);
                if (result.getJobType().equals(JobNotificationType.BUILD_CONFIG_CREATION)) {
                    buildConfigurationProvider.createBuildConfigurationWithRepository(
                            result.getTaskId().toString(),
                            notification.getRepositoryId(),
                            result.getBuildConfiguration());

                }
                notifySCMRepositoryCreated(notification);
            }
        } else {
            String eventType;
            if (!result.isRepoCreatedSuccessfully()) {
                eventType = BpmEventType.RC_REPO_CREATION_ERROR.toString();
            } else {
                eventType = BpmEventType.RC_REPO_CLONE_ERROR.toString();
            }

            org.jboss.pnc.bpm.model.RepositoryConfiguration repositoryConfiguration = org.jboss.pnc.bpm.model.RepositoryConfiguration
                    .builder()
                    .externalUrl(result.getExternalUrl())
                    .preBuildSyncEnabled(result.isPreBuildSyncEnabled())
                    .build();
            RepositoryCreationProcess repositoryCreationProcess = RepositoryCreationProcess.builder()
                    .repositoryConfiguration(repositoryConfiguration)
                    .build();
            notifier.sendMessage(
                    new RepositoryCreationFailure(
                            result.getJobType(),
                            eventType,
                            repositoryCreationProcess,
                            result.getTaskId().toString()));
        }
    }
}
