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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.auth.AuthenticationProvider;
import org.jboss.pnc.auth.ExternalAuthentication;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.AuthenticationModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.integration.utils.AuthResource;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.pnc.test.category.RemoteTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.jboss.pnc.integration.env.IntegrationTestEnv.getHttpPort;

@RunWith(Arquillian.class)
@Category({ContainerTest.class, RemoteTest.class})
public class BuildTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static AuthenticationProvider authProvider;
    private static String access_token =  "no-auth";


    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();
        WebArchive war = enterpriseArchive.getAsType(WebArchive.class, "/rest.war");
        war.addClass(BuildTest.class);
        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }
    
    @BeforeClass
    public static void setupAuth() throws IOException, ConfigurationParseException {
        if(AuthResource.authEnabled()) {
            Configuration configuration = new Configuration();
            AuthenticationModuleConfig config = configuration.getModuleConfig(new PncConfigProvider<AuthenticationModuleConfig>(AuthenticationModuleConfig.class));
            InputStream is = BuildRecordRestTest.class.getResourceAsStream("/keycloak.json");
            ExternalAuthentication ea = new ExternalAuthentication(is);
            authProvider = ea.authenticate(config.getUsername(), config.getPassword());
            access_token = authProvider.getTokenString();
        }
    }

    @Test
    public void shouldTriggerBuildAndFinishWithoutProblems() {
        int configurationId = extractIdFromRest("/pnc-rest/rest/build-configurations");

        given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    
            .port(getHttpPort())
        .when()
            .post(String.format("/pnc-rest/rest/build-configurations/%d/build", configurationId))
        .then()
            .statusCode(200);
    }

    @Test
    public void shouldTriggerBuildSetAndFinishWithoutProblems() {
        int configurationId = extractIdFromRest("/pnc-rest/rest/build-configuration-sets");

        given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)

            .port(getHttpPort())
        .when()
            .post(String.format("/pnc-rest/rest/build-configuration-sets/%d/build", configurationId))
        .then()
            .statusCode(200);
    }

    Integer extractIdFromRest(String path) {
        String returnedObject = from(given().header("Accept", "application/json").header("Authorization", "Bearer " + access_token)
                    
                .port(getHttpPort()).get(path).asString()).get("content[0].id").toString();
        return Integer.valueOf(returnedObject);
    }
}
