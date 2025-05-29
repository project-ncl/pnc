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
package org.jboss.pnc.remotecoordinator.maintenance;

import org.commonjava.indy.client.core.Indy;
import org.commonjava.indy.client.core.IndyClientException;
import org.commonjava.indy.client.core.module.IndyStoresClientModule;
import org.commonjava.indy.folo.client.IndyFoloAdminClientModule;
import org.commonjava.indy.model.core.BatchDeleteRequest;
import org.commonjava.indy.model.core.Group;
import org.commonjava.indy.model.core.StoreKey;
import org.commonjava.indy.model.core.StoreType;
import org.commonjava.indy.model.core.dto.StoreListingDTO;
import org.commonjava.indy.promote.client.IndyPromoteAdminClientModule;
import org.jboss.pnc.api.causeway.dto.untag.TaggedBuild;
import org.jboss.pnc.api.causeway.dto.untag.UntagRequest;
import org.jboss.pnc.api.enums.OperationResult;
import org.jboss.pnc.auth.KeycloakServiceClient;
import org.jboss.pnc.causewayclient.CausewayClient;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.IndyRepoDriverModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.enums.BuildType;
import org.jboss.pnc.enums.ResultStatus;
import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.model.BuildPushOperation;
import org.jboss.pnc.model.BuildPushReport;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.spi.coordinator.Result;
import org.jboss.pnc.spi.datastore.predicates.BuildPushPredicates;
import org.jboss.pnc.spi.datastore.predicates.OperationPredicates;
import org.jboss.pnc.spi.datastore.repositories.BuildPushOperationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.List;

import static org.commonjava.indy.pkg.PackageTypeConstants.PKG_TYPE_GENERIC_HTTP;
import static org.commonjava.indy.pkg.PackageTypeConstants.PKG_TYPE_MAVEN;
import static org.commonjava.indy.pkg.PackageTypeConstants.PKG_TYPE_NPM;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Dependent
public class DefaultRemoteBuildsCleaner implements RemoteBuildsCleaner {

    private final Logger logger = LoggerFactory.getLogger(DefaultRemoteBuildsCleaner.class);

    private KeycloakServiceClient serviceClient;

    private CausewayClient causewayClient;

    private final BuildPushOperationRepository buildPushOperationRepository;

    private final String tempBuildPromotionGroup;

    private final Indy indy;

    private final RefreshingIndyAuthenticator refreshingIndyAuthenticator;

    @Inject
    public DefaultRemoteBuildsCleaner(
            Configuration configuration,
            Indy indy,
            KeycloakServiceClient serviceClient,
            CausewayClient causewayClient,
            BuildPushOperationRepository buildPushOperationRepository,
            RefreshingIndyAuthenticator refreshingIndyAuthenticator) {
        this.indy = indy;
        this.serviceClient = serviceClient;
        this.causewayClient = causewayClient;
        this.buildPushOperationRepository = buildPushOperationRepository;
        this.refreshingIndyAuthenticator = refreshingIndyAuthenticator;

        IndyRepoDriverModuleConfig config;
        try {
            config = configuration.getModuleConfig(new PncConfigProvider<>(IndyRepoDriverModuleConfig.class));
        } catch (ConfigurationParseException e) {
            throw new IllegalStateException(
                    "Cannot read configuration for " + IndyRepoDriverModuleConfig.class.getName() + ".",
                    e);
        }
        this.tempBuildPromotionGroup = config.getTempBuildPromotionTarget();
    }

    @Override
    public Result deleteRemoteBuilds(BuildRecord buildRecord) {
        Result result = deleteBuildsFromIndy(buildRecord);
        if (!result.isSuccess()) {
            return result;
        }
        result = requestDeleteViaCauseway(buildRecord);
        if (!result.isSuccess()) {
            return result;
        }
        return new Result(BuildMapper.idMapper.toDto(buildRecord.getId()), ResultStatus.SUCCESS);
    }

    private Result requestDeleteViaCauseway(BuildRecord buildRecord) {
        List<BuildPushOperation> buildPushOperations = buildPushOperationRepository.queryWithPredicates(
                BuildPushPredicates.withBuild(buildRecord.getId()),
                OperationPredicates.withResult(OperationResult.SUCCESSFUL));
        String externalBuildId = BuildMapper.idMapper.toDto(buildRecord.getId());
        for (BuildPushOperation buildPushOperation : buildPushOperations) {
            BuildPushReport report = buildPushOperation.getReport();
            boolean success = causewayUntag(report.getOperation().getTagPrefix(), report.getBrewBuildId());
            if (!success) {
                logger.error(
                        "Failed to un-tag pushed build record. BuildRecord.id: {}; brewBuildId: {}; tagPrefix: {};",
                        buildRecord.getId(),
                        report.getBrewBuildId(),
                        report.getOperation().getTagPrefix());
                return new Result(externalBuildId, ResultStatus.FAILED, "Failed to un-tag pushed build record.");
            }
        }
        return new Result(externalBuildId, ResultStatus.SUCCESS);
    }

    private Result deleteBuildsFromIndy(BuildRecord buildRecord) {
        String buildContentId = buildRecord.getBuildContentId();
        BuildType buildType = buildRecord.getBuildConfigurationAudited().getBuildType();
        String pkgKey = getRepoPkgKey(buildType);

        Result result;
        if (buildContentId == null) {
            logger.debug("Build contentId is null. Nothing to be deleted from Indy.");
            return new Result(
                    buildContentId,
                    ResultStatus.SUCCESS,
                    "BuildContentId is null. Nothing to be deleted from Indy.");
        }

        try {
            IndyStoresClientModule indyStores = indy.stores();
            if (pkgKey != null) {
                // delete artifacts from consolidated repository
                // (failed builds are not promoted and don't have artifacts in consolidated repository)
                if (buildRecord.getStatus().completedSuccessfully()) {
                    StoreKey tempHostedKey = new StoreKey(pkgKey, StoreType.hosted, tempBuildPromotionGroup);

                    BatchDeleteRequest request = new BatchDeleteRequest();
                    request.setTrackingID(buildContentId);
                    request.setStoreKey(tempHostedKey);
                    indy.module(IndyFoloAdminClientModule.class).deleteFilesFromStoreByTrackingID(request);
                }

                // delete the content
                StoreKey storeKey = new StoreKey(pkgKey, StoreType.hosted, buildContentId);
                indyStores.delete(storeKey, "Scheduled cleanup of temporary builds.", true);
            }
            // delete generic http repos
            List<Group> genericGroups;
            try {
                StoreListingDTO<Group> groupListing = indyStores.listGroups(PKG_TYPE_GENERIC_HTTP);
                genericGroups = groupListing.getItems();

                for (Group genericGroup : genericGroups) {
                    if (genericGroup.getName().startsWith("g-")
                            && genericGroup.getName().endsWith("-" + buildContentId)) {
                        deleteRepoGroup(indyStores, genericGroup);
                    }
                }
            } catch (IndyClientException e) {
                logger.error("Error in deleting generic http repos for build {}.", buildContentId, e);
            }

            // delete the tracking record
            IndyFoloAdminClientModule foloAdmin = indy.module(IndyFoloAdminClientModule.class);
            foloAdmin.clearTrackingRecord(buildContentId);

            try {
                // try to delete the promotion tracking record
                IndyPromoteAdminClientModule promoteAdmin = indy.module(IndyPromoteAdminClientModule.class);
                promoteAdmin.deleteTrackingRecords(buildContentId);
            } catch (IndyClientException e) {
                logger.warn(
                        "Removal of promotion-tracking records for build {} unsuccessful, the build may have not"
                                + " gone through promotion.",
                        buildContentId,
                        e);
            }

            result = new Result(buildContentId, ResultStatus.SUCCESS);
            logger.debug("Indy content for buildContentID {} deleted successfully.", buildContentId);
        } catch (IndyClientException e) {
            String description = MessageFormat.format(
                    "Failed to delete temporary hosted repository identified by buildContentId {0}.",
                    buildContentId);
            logger.error(description, e);
            result = new Result(buildContentId, ResultStatus.FAILED, description);
        }

        return result;
    }

    /**
     * Gets the Indy package key based on build type. It reads the PNC repo type from the build type and translates it
     * into the Indy package type.
     *
     * @param buildType the build type, cannot be {@code null}
     * @return the package key or {@code null} in case an unsupported build type is passed in
     */
    private String getRepoPkgKey(BuildType buildType) {
        switch (buildType.getRepoType()) {
            case MAVEN:
                return PKG_TYPE_MAVEN;

            case NPM:
                return PKG_TYPE_NPM;

            default:
                logger.error("No default repository type for build type {}.", buildType);
                return null;
        }
    }

    private void deleteRepoGroup(IndyStoresClientModule indyStores, Group group) throws IndyClientException {
        for (StoreKey constituent : group.getConstituents()) {
            if (StoreType.group == constituent.getType()) {
                Group subgroup = indyStores.load(constituent, Group.class);
                deleteRepoGroup(indyStores, subgroup);
            } else {
                indyStores.delete(constituent, "Scheduled cleanup of temporary builds.");
            }
        }
        indyStores.delete(group.getKey(), "Scheduled cleanup of temporary builds.");
    }

    private boolean causewayUntag(String tagPrefix, int brewBuildId) {
        String authToken = serviceClient.getAuthToken();

        UntagRequest untagRequest = prepareUntagRequest(tagPrefix, brewBuildId);
        return causewayClient.untagBuild(untagRequest, authToken);
    }

    private UntagRequest prepareUntagRequest(String tagPrefix, int brewBuildId) {
        TaggedBuild taggedBuild = new TaggedBuild(tagPrefix, brewBuildId);
        return new UntagRequest(null, taggedBuild);
    }

}
