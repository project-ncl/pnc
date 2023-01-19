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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by aabulawi on 10/07/15.
 */

@RunWith(Arquillian.class)
@Ignore // TODO if needed move to integration tests
public class ProjectWithFailedDependenciesBuildTest extends ProjectBuilder {

    @Inject
    BuildCoordinatorFactory buildCoordinatorFactory;

    @Deployment
    public static JavaArchive createDeployment() {
        return BuildCoordinatorDeployments.deployment(
                BuildCoordinatorDeployments.Options.WITH_DATASTORE,
                BuildCoordinatorDeployments.Options.WITH_BPM);
    }

    @Test
    @InSequence(10)
    public void buildFailingProjectTestCase() throws Exception {
        BuildCoordinatorBeans buildCoordinatorBeans = buildCoordinatorFactory.createBuildCoordinator(datastore);

        buildFailingProject(
                configurationBuilder.buildConfigurationSetWithFailedDependencies(1),
                1,
                buildCoordinatorBeans.coordinator,
                buildCoordinatorBeans.setJob);
    }

    @Test
    @InSequence(20)
    public void checkDatabaseForResult() {
        List<BuildRecord> buildRecords = datastore.getBuildRecords();

        Assert.assertEquals("Wrong datastore results count. Got records: " + buildRecords, 2, buildRecords.size());
        Assert.assertEquals(BuildStatus.FAILED, buildRecords.get(0).getStatus());
        Assert.assertEquals(BuildStatus.REJECTED_FAILED_DEPENDENCIES, buildRecords.get(1).getStatus());

        BuildConfigSetRecord buildConfigSetRecord = datastore.getBuildConfigSetRecords().get(0);
        Assert.assertNotNull("End time is null.", buildConfigSetRecord.getEndTime());
        Assert.assertTrue(buildConfigSetRecord.getEndTime().getTime() > buildConfigSetRecord.getStartTime().getTime());
        Assert.assertEquals(BuildStatus.FAILED, buildConfigSetRecord.getStatus());

    }

}
