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
package org.jboss.pnc.integration;

import com.jayway.restassured.response.Response;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.integration.client.RepositoryCreationRestClient;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.rest.provider.BuildConfigurationProvider;
import org.jboss.pnc.rest.provider.RepositoryConfigurationProvider;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.RepositoryConfigurationRest;
import org.jboss.pnc.rest.restmodel.bpm.RepositoryCreationRest;
import org.jboss.pnc.rest.restmodel.bpm.RepositoryCreationResultRest;
import org.jboss.pnc.rest.restmodel.mock.RepositoryCreationRestMockBuilder;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class RepositoryCreationRestTest {

    public static final Logger logger = LoggerFactory.getLogger(RepositoryCreationRestTest.class);

    private static final String INTERNAL_REPO = "git+ssh://git-repo-user@git-repo.devvm.devcloud.example.com:12839";
    private static final String INTERNAL_SCM_URL = INTERNAL_REPO + "/my/repo.git";

    RepositoryCreationRestClient repositoryCreationRestClient;

    @Inject
    RepositoryConfigurationProvider repositoryConfigurationProvider;

    @Inject
    BuildConfigurationProvider buildConfigurationProvider;

    private String buildConfigurationName;

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();
        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Before
    public void before() {
        if(repositoryCreationRestClient == null) {
            repositoryCreationRestClient = new RepositoryCreationRestClient();
        }
    }

    @Test
    public void shouldCreateRCAndBC() {
        //given
        String buildConfigurationName = "pnc-1.1";
        RepositoryCreationRest repositoryCreationRest = RepositoryCreationRestMockBuilder.mock(buildConfigurationName, "mvn clean deploy", INTERNAL_SCM_URL);

        //when
        Response response = repositoryCreationRestClient.createNewRCAndBC(repositoryCreationRest);

        //expect
        Assert.assertEquals(200, response.statusCode());

        RepositoryConfigurationRest retrievedRepositoryConfig = repositoryConfigurationProvider.getSpecificByInternalScm(INTERNAL_SCM_URL);
        Assert.assertEquals(INTERNAL_SCM_URL, retrievedRepositoryConfig.getInternalScmRepoUrl());

        RepositoryCreationResultRest result = response.jsonPath().getObject("", RepositoryCreationResultRest.class);

        BuildConfigurationRest retrievedBC = buildConfigurationProvider.getSpecific(result.getBuildConfigurationId());
        Assert.assertEquals(buildConfigurationName, retrievedBC.getName());
    }


}
