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
package org.jboss.pnc.core.test.buildCoordinator;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildStatus;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * Created by aabulawi on 10/07/15.
 */

@RunWith(Arquillian.class)
public class ProjectWithFailedDependenciesBuildTest extends ProjectBuilder {

    @Test
    @InSequence(10)
    public void buildFailingProjectTestCase() throws Exception {
        buildFailingProject(configurationBuilder.buildConfigurationSetWithFailedDependencies(1), 1, 1);
    }

    @Test
    @InSequence(20)
    public void checkDatabaseForResult() {
        List<BuildRecord> buildRecords = datastore.getBuildRecords();

        Assert.assertEquals("Wrong datastore results count.", 2, buildRecords.size());
        Assert.assertEquals(BuildStatus.FAILED, buildRecords.get(0).getStatus());
        Assert.assertEquals(BuildStatus.REJECTED, buildRecords.get(1).getStatus());

        BuildConfigSetRecord buildConfigSetRecord = datastore.getBuildConfigSetRecords().get(0);
        System.out.println("status of failed buildconfigset: " + buildConfigSetRecord.getStatus());
        Assert.assertNotNull("End time is null.", buildConfigSetRecord.getEndTime());
        Assert.assertTrue(buildConfigSetRecord.getEndTime().getTime() > buildConfigSetRecord.getStartTime().getTime());
        Assert.assertEquals(BuildStatus.FAILED, buildConfigSetRecord.getStatus());



    }

}
