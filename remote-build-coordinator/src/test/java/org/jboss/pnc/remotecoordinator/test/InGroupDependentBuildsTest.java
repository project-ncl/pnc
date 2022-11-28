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
package org.jboss.pnc.remotecoordinator.test;

import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.mock.repository.BuildConfigurationRepositoryMock;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.enums.RebuildMode;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.exception.CoreException;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * configC depends on configB, which in turn depends on configA. configD depends on configA and configB configE doesn't
 * have dependencies
 *
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 9/14/16 Time: 12:09 PM
 */
public class InGroupDependentBuildsTest extends AbstractDependentBuildTest {

    private BuildConfiguration configA;

    private BuildConfiguration configB;
    private BuildConfiguration configC;
    private BuildConfiguration configD;
    private BuildConfiguration configE;

    private BuildConfigurationSet configSet;

    @Before
    public void initialize() throws DatastoreException, ConfigurationParseException {
        configA = buildConfig("A");

        configB = buildConfig("B", configA);
        configC = buildConfig("C", configB);
        configD = buildConfig("D", configA, configB);
        configE = buildConfig("E");

        configSet = configSet(configA, configB, configC, configD, configE);

        buildConfigurationRepository = spy(new BuildConfigurationRepositoryMock());
        when(buildConfigurationRepository.queryWithPredicates(any()))
                .thenReturn(new ArrayList<>(configSet.getBuildConfigurations()));

        super.initialize();

        configSet.getBuildConfigurations().forEach(this::saveConfig);
    }

    @Test
    public void shouldBuildAllIfNotSuccessfullyBuilt() throws CoreException, TimeoutException, InterruptedException {
        build(configSet, RebuildMode.IMPLICIT_DEPENDENCY_CHECK);

        assertThat(getBuiltConfigs()).hasSameElementsAs(asList(configA, configB, configC, configD, configE));
    }

    @Test
    public void shouldNotCreateTaskForNonDependentBuilt() throws CoreException, TimeoutException, InterruptedException {
        insertNewBuildRecords(configE);
        build(configSet, RebuildMode.IMPLICIT_DEPENDENCY_CHECK);

        assertThat(getBuiltConfigs()).hasSameElementsAs(asList(configA, configB, configC, configD));
    }

    @Test
    public void shouldCreateTaskForNonDependentBuiltWithRebuildAll()
            throws CoreException, TimeoutException, InterruptedException {
        insertNewBuildRecords(configE);
        build(configSet, RebuildMode.FORCE);

        assertThat(getBuiltConfigs()).hasSameElementsAs(asList(configA, configB, configC, configD, configE));
    }

    @Test
    public void shouldCreateTaskForDependentBuilt() throws CoreException, TimeoutException, InterruptedException {
        insertNewBuildRecords(configA, configC, configD, configE);
        build(configSet, RebuildMode.IMPLICIT_DEPENDENCY_CHECK);

        assertThat(getBuiltConfigs()).hasSameElementsAs(asList(configB, configC, configD));
    }

    @Test
    public void shouldBuildOnlyCWhenOnlyCIsUpdated() throws CoreException, TimeoutException, InterruptedException {
        insertNewBuildRecords(configA, configB, configC, configD, configE);

        updateConfiguration(configC);

        build(configSet, RebuildMode.EXPLICIT_DEPENDENCY_CHECK);

        expectBuilt(configC);
    }

    @Test
    public void shouldBuildBCDWhenBIsUpdated() throws CoreException, TimeoutException, InterruptedException {
        insertNewBuildRecords(configA, configB, configC, configD, configE);

        updateConfiguration(configB);

        build(configSet, RebuildMode.EXPLICIT_DEPENDENCY_CHECK);

        expectBuilt(configB, configC, configD);
    }
}