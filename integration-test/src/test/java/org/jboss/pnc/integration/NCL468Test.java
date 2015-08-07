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

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.jboss.pnc.integration.env.IntegrationTestEnv.getHttpPort;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;

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
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.restassured.http.ContentType;

import groovy.json.StringEscapeUtils;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class NCL468Test {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static AuthenticationProvider authProvider;
    private static String access_token =  "no-auth";

    @Deployment(name="NCL468Test", testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();
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
    public void shouldGetAllUsers() {
        final String toMatch = "[{\"id\":1,\"email\":\"demo-user@pnc.com\",\"firstName\":\"Demo First Name\",\"lastName\":\"Demo Last Name\",\"username\":\"demo-user\"}]";

        given()
        .header("Accept", "application/json")
        .header("Authorization", "Bearer " + access_token)
        .contentType(ContentType.JSON).port(getHttpPort()).when().get("/pnc-rest/rest/users")
            .then().assertThat().body(
                equalTo(StringEscapeUtils.unescapeJava(toMatch)));
    }
}
