/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.coordinator.maintenance;

import org.apache.commons.io.IOUtils;
import org.commonjava.indy.client.core.Indy;
import org.commonjava.indy.client.core.IndyClientException;
import org.commonjava.indy.folo.client.IndyFoloAdminClientModule;
import org.commonjava.indy.model.core.StoreKey;
import org.commonjava.indy.model.core.StoreType;
import org.jboss.pnc.auth.KeycloakServiceClient;
import org.jboss.pnc.causewayclient.CausewayClient;
import org.jboss.pnc.causewayclient.remotespi.TaggedBuild;
import org.jboss.pnc.causewayclient.remotespi.UntagRequest;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecordPushResult;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordPushResultRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Dependent
public class DefaultRemoteBuildsCleaner implements RemoteBuildsCleaner {

    private Logger logger = LoggerFactory.getLogger(DefaultRemoteBuildsCleaner.class);

    public static final String MAVEN_PKG_KEY = "maven";

    private IndyFactory indyFactory;

    KeycloakServiceClient serviceClient;

    CausewayClient causewayClient;

    @Inject
    public DefaultRemoteBuildsCleaner(
            IndyFactory indyFactory,
            KeycloakServiceClient serviceClient,
            CausewayClient causewayClient) {
        this.indyFactory = indyFactory;
        this.serviceClient = serviceClient;
        this.causewayClient = causewayClient;
    }

    @Override
    public boolean deleteRemoteBuilds(BuildRecord buildRecord, String authToken) {
        if (!deleteBuildsFromIndy(buildRecord.getBuildContentId(), authToken)) {
            return false;
        }
        if (!requestDeleteViaCauseway(buildRecord)) {
            return false;
        }
        return true;
    }

    private boolean requestDeleteViaCauseway(BuildRecord buildRecord) {
        BuildRecordPushResultRepository buildRecordPushResultRepository=null;
        List<BuildRecordPushResult> toRemove = buildRecordPushResultRepository.getAllSuccessfulForBuildRecord(buildRecord.getId());

        for (BuildRecordPushResult pushResult : toRemove) {
            boolean success = causewayUntag(pushResult.getTagPrefix(), pushResult.getBrewBuildId());
            if (!success) {
                logger.error("Failed to un-tag pushed build record. BuildRecord.id: {}; brewBuildId: {}; tagPrefix: {};",
                        buildRecord.getId(), pushResult.getBrewBuildId(), pushResult.getTagPrefix());
                return false;
            }
        }
        return true;
    }

    private boolean deleteBuildsFromIndy(String buildContentId, String authToken) {
        Indy indy = indyFactory.get(authToken);
        try {
            //delete the content
            StoreKey storeKey = new StoreKey(MAVEN_PKG_KEY, StoreType.hosted, buildContentId);
            indy.stores().delete(storeKey, "Scheduled cleanup of temporary builds.");

            //delete the tracking record
            IndyFoloAdminClientModule foloAdmin = indy.module(IndyFoloAdminClientModule.class);
            foloAdmin.clearTrackingRecord(buildContentId);
        } catch (IndyClientException e) {
            logger.error("Failed to delete temporary hosted repository identified by buildContentId {}." + buildContentId, e);
        } finally {
            IOUtils.closeQuietly(indy);
        }
        return true;
    }

    private boolean causewayUntag(String tagPrefix, int brewBuildId) {
        String authToken = serviceClient.getAuthToken();

        UntagRequest untagRequest = prepareUntagRequest(tagPrefix, brewBuildId);
        return causewayClient.untagBuild(untagRequest, authToken);
    }

    private UntagRequest prepareUntagRequest(String tagPrefix, int brewBuildId) {
        TaggedBuild taggedBuild = new TaggedBuild(tagPrefix, brewBuildId);
        return new UntagRequest(taggedBuild, null);
    }
}
