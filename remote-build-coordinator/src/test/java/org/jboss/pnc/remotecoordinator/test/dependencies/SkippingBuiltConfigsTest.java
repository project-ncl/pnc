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
package org.jboss.pnc.remotecoordinator.test.dependencies;

import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.mock.repository.BuildConfigurationRepositoryMock;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 9/22/16 Time: 2:51 PM
 */
public class SkippingBuiltConfigsTest extends AbstractDependentBuildTest {
    // TODO move the tests that don't relly on the results to other classes
    // TODO drop the test that depend on result storing as the result storing should be tested on it's own

    private static final Logger log = LoggerFactory.getLogger(SkippingBuiltConfigsTest.class);

    private BuildConfiguration configA;

    private BuildConfiguration configB;
    private BuildConfiguration configC;
    private BuildConfiguration configD;
    private BuildConfiguration configE;

    private BuildConfigurationSet configSet;

    @Before
    public void initialize() throws DatastoreException, ConfigurationParseException {
        configA = buildConfig("A");
        configB = buildConfig("B");
        configC = buildConfig("C");
        configD = buildConfig("D");
        configE = buildConfig("E");

        configSet = configSet(configA, configB, configC, configD, configE);

        buildConfigurationRepository = spy(new BuildConfigurationRepositoryMock());
        when(buildConfigurationRepository.queryWithPredicates(any()))
                .thenReturn(new ArrayList<>(configSet.getBuildConfigurations()));

        super.initialize();

        configSet.getBuildConfigurations().forEach(this::saveConfig);
    }

    @Test
    @Ignore // TODO what is this testing?
    public void shouldBuildConfigurationAndUnbuiltDependency() throws Exception {
        buildRecordRepository.clear();
        // given
        BuildConfiguration testConfiguration = config("shouldBuildConfigurationAndUnbuiltDependency");
        BuildConfiguration dependency = config("dependency");
        testConfiguration.addDependency(dependency);
        BuildOptions buildOptions = new BuildOptions();

        // when
        coordinator.buildConfig(testConfiguration, user, buildOptions);

        // then
        assertThat(getNonRejectedBuildRecords().size()).isEqualTo(2);
    }

    private List<BuildRecord> getNonRejectedBuildRecords() {
        return buildRecordRepository.queryAll()
                .stream()
                .filter(r -> r.getStatus() != BuildStatus.REJECTED && r.getStatus() != BuildStatus.NO_REBUILD_REQUIRED)
                .collect(Collectors.toList());
    }

}
