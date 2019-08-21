/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.client.Configuration;
import org.jboss.pnc.client.ProjectClient;
import org.jboss.pnc.dto.Project;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
@RunAsClient
public class ProjectRestTestNewClient {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static ProjectClient projectClient;

    private static final Configuration.BasicAuth basicAuth = new Configuration.BasicAuth("admin", "user.1234");

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();
        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Before
    public void before() {
        if(projectClient == null) {
            Configuration configuration = Configuration.builder()
                    .protocol("http")
                    .host("localhost")
                    .port(8080)
                    .basicAuth(basicAuth)
                    .build();
            projectClient = new ProjectClient(configuration);
        }
    }

    @Test @Ignore //TODO enable
    public void shouldInsertNewProject() throws Exception {
        //given
        Project project = Project.builder()
                .projectUrl("https://github.com/entity-ncl/pnc")
                .issueTrackerUrl("https://github.com/entity-ncl/pnc/issues")
                .build();

        //when
        Project projectReturned = projectClient.createNew(project);

        //than
        Integer returnedId = Integer.valueOf(projectReturned.getId());
        Assert.assertNotNull(returnedId);
    }

    @Test @Ignore //TODO enable
    public void shouldUpdateOnlySomeFields() throws Exception {
        //given
        int id = 1; //TODO get id

        Project project = Project.builder()
                .description("Amazing project!")
                .build();

        //when
        projectClient.update(id, project);

        //than
        //non specified fields should be untouched
    }

    @Test @Ignore //TODO enable
    public void shouldReadAllThelist() throws Exception {
        //given
        //-- save lots of projects

        //when
        for (Project project1 : projectClient.getAll()) {

        }

        //than
        //non specified fields should be untouched
    }

}
