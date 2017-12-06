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
import org.commonjava.indy.model.core.StoreKey;
import org.commonjava.indy.model.core.StoreType;
import org.jboss.pnc.auth.KeycloakClient;
import org.jboss.pnc.auth.KeycloakServiceClient;
import org.jboss.pnc.coordinator.BuildCoordinationException;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Dependent
public class TemporaryBuildCleaner {
    public static final String MAVEN_PKG_KEY = "maven";

    private IndyFactory indyFactory;

    @Inject
    public TemporaryBuildCleaner(IndyFactory indyFactory) {
        this.indyFactory = indyFactory;
    }

    public void deleteBuildsFromIndy(String buildContentId) throws BuildCoordinationException {


        Indy indy = indyFactory.get(accessToken);
        try {
            StoreKey storeKey = new StoreKey(MAVEN_PKG_KEY, StoreType.hosted, buildContentId);
            indy.stores().delete(storeKey, "Scheduled cleanup of temporary builds.");

        } catch (IndyClientException e) {
            throw new BuildCoordinationException("Failed to delete temporary hosted repository identied by buildContentId: " + buildContentId, e);
        } finally {
            IOUtils.closeQuietly(indy);
        }
    }

}
