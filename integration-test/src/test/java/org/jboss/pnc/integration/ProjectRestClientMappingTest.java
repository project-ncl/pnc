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

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.pnc.AbstractTest;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.rest.restmodel.ProjectRest;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Arquillian.class)
@Transactional(TransactionMode.ROLLBACK)
@Category(ContainerTest.class)
public class ProjectRestClientMappingTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Inject
    private BuildConfigurationRepository buildConfigurationRepository;

    private int configurationId;

    @Deployment
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEarWithTestDependencies();
        WebArchive war = enterpriseArchive.getAsType(WebArchive.class, AbstractTest.REST_WAR_PATH);
        war.addClass(ProjectRestClientMappingTest.class);
        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Test
    @InSequence(-1)
    public void prepareTestData() {
        BuildConfiguration buildConfiguration = buildConfigurationRepository.queryAll().get(0);
        configurationId = buildConfiguration.getId();
    }

    @Test
    public void shouldRemapProjectRestToProject() {
        // given
        ProjectRest projectRest = new ProjectRest();
        projectRest.setId(1);
        projectRest.setConfigurationIds(Arrays.asList(2));
        projectRest.setDescription("description");
        projectRest.setIssueTrackerUrl("issueTracker");
        projectRest.setName("name");
        projectRest.setProjectUrl("projectUrl");

        // when
        Project project = projectRest.toDBEntityBuilder().build();
        List<Integer> buildConfigurationIds = project.getBuildConfigurations()
                .stream()
                .map(buildConfiguration -> buildConfiguration.getId())
                .collect(Collectors.toList());

        // then
        assertThat(project.getId()).isEqualTo(1);
        assertThat(project.getDescription()).isEqualTo("description");
        assertThat(project.getIssueTrackerUrl()).isEqualTo("issueTracker");
        assertThat(project.getName()).isEqualTo("name");
        assertThat(project.getProjectUrl()).isEqualTo("projectUrl");
        assertThat(buildConfigurationIds).containsExactly(2);
    }

}
