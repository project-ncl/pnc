/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.integration;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.client.ConnectionInfo;
import org.jboss.pnc.client.ProjectClient;
import org.jboss.pnc.dto.Project;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class ProjectRestTestNewClient {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static ProjectClient projectClient;

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();
        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Before
    public void before() {
        if(projectClient == null) {
            ConnectionInfo connectionInfo = ConnectionInfo.newBuilder()
                    .host("localhost")
                    .port(8080)
                    .build();
            projectClient = new ProjectClient(connectionInfo);
        }
    }

    @Test
    public void shouldInsertNewProject() throws Exception {
        //given
        Project project = Project.builder()
                .projectUrl("https://github.com/entity-ncl/pnc")
                .issueTrackerUrl("https://github.com/entity-ncl/pnc/issues")
                .build();

        //when
        Project projectReturned = projectClient.createNew(project);

        //than
        Integer returnedId = projectReturned.getId();
        Assert.assertNotNull(returnedId);
    }

}
