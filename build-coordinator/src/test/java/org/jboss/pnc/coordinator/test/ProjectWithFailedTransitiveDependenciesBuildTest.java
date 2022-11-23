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
package org.jboss.pnc.coordinator.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.coordinator.builder.SetRecordUpdateJob;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.mock.model.builders.TestProjectConfigurationBuilder;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.datastore.BuildTaskRepository;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.List;

import static org.jboss.pnc.common.Configuration.CONFIG_SYSPROP;

/**
 * Created by aabulawi on 10/07/15.
 */

@RunWith(Arquillian.class)
@Ignore // SHOULD BE DONE IN INTEGRATION TESTS WITH REX
public class ProjectWithFailedTransitiveDependenciesBuildTest extends ProjectBuilder {

    @Inject
    BuildCoordinatorFactory buildCoordinatorFactory;

    static BuildTaskRepository taskRepository;

    @Deployment
    public static JavaArchive createDeployment() {
        JavaArchive deployment = BuildCoordinatorDeployments.deployment(
                BuildCoordinatorDeployments.Options.WITH_DATASTORE,
                BuildCoordinatorDeployments.Options.WITH_BPM);
        deployment.addAsResource("pnc-config-one-parallel-only.json");
        System.setProperty(CONFIG_SYSPROP, "pnc-config-one-parallel-only.json");
        return deployment;
    }

    @Test
    @InSequence(10)
    public void buildFailingProjectTestCase() throws Exception {
        TestProjectConfigurationBuilder configurationBuilder = new TestProjectConfigurationBuilder(datastore);
        BuildCoordinatorBeans beans = buildCoordinatorFactory.createBuildCoordinator(datastore);
        BuildCoordinator coordinator = beans.coordinator;
        SetRecordUpdateJob setJob = beans.setJob;
        taskRepository = beans.taskRepository;

        buildFailingProject(
                configurationBuilder.buildConfigurationSetWithFailedDependenciesAndDelay(1),
                1,
                2,
                coordinator,
                setJob);
    }

    @Test
    @InSequence(20)
    public void checkDatabaseForResult() {
        List<BuildRecord> buildRecords = datastore.getBuildRecords();

        Assert.assertEquals("Wrong datastore results count. Got records: " + buildRecords, 3, buildRecords.size());
        Assert.assertEquals(BuildStatus.FAILED, buildRecords.get(0).getStatus());
        Assert.assertEquals(BuildStatus.REJECTED_FAILED_DEPENDENCIES, buildRecords.get(1).getStatus());

        BuildConfigSetRecord buildConfigSetRecord = datastore.getBuildConfigSetRecords().get(0);
        Assert.assertNotNull("End time is null.", buildConfigSetRecord.getEndTime());
        Assert.assertTrue(buildConfigSetRecord.getEndTime().getTime() > buildConfigSetRecord.getStartTime().getTime());
        Assert.assertEquals(BuildStatus.FAILED, buildConfigSetRecord.getStatus());

        Assert.assertTrue("Build taskRepository should be empty.", taskRepository.isEmpty());

    }

}
