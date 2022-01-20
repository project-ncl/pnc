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
package org.jboss.pnc.indyrepositorymanager;

import org.commonjava.indy.client.core.Indy;
import org.jboss.pnc.common.json.moduleconfig.IndyRepoDriverModuleConfig;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.indyrepositorymanager.fixture.TestBuildExecution;
import org.jboss.pnc.spi.repositorymanager.BuildExecution;
import org.jboss.pnc.spi.repositorymanager.model.RepositoryConnectionInfo;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.jboss.pnc.test.category.ContainerTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@Category(ContainerTest.class)
public class BuildGroupIndySidecarDeActivatedTest extends AbstractRepositoryManagerDriverTest {

    String sideCarUrl = "https://indy-sidecar:824";

    /**
     * This is called by the base class before configuration of the driver
     *
     * @param config
     */
    @Override
    public void preConfigureMavenRepositoryDriver(IndyRepoDriverModuleConfig config) {
        config.setIndySidecarEnabled(false);
        config.setIndySidecarUrl(sideCarUrl);
    }

    @Test
    public void verifyGroupComposition_ProductVersion_WithConfSet() throws Exception {
        // create a dummy composed (chained) build execution and a repo session based on it
        BuildExecution execution = new TestBuildExecution("build_myproject_1111");
        Indy indy = driver.getIndy(accessToken);

        RepositorySession repositoryConfiguration = driver.createBuildRepository(
                execution,
                accessToken,
                accessToken,
                RepositoryType.MAVEN,
                Collections.emptyMap(),
                false);

        RepositoryConnectionInfo info = repositoryConfiguration.getConnectionInfo();
        assertThat(info.getDependencyUrl()).doesNotStartWith(sideCarUrl);
        assertThat(info.getDeployUrl()).doesNotStartWith(sideCarUrl);
    }
}
