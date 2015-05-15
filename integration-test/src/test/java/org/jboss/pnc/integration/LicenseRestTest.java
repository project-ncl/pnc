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

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.auth.AuthenticationProvider;
import org.jboss.pnc.auth.ExternalAuthentication;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.AuthenticationModuleConfig;
import org.jboss.pnc.integration.Utils.AuthResource;
import org.jboss.pnc.integration.Utils.ResponseUtils;
import org.jboss.pnc.integration.assertions.ResponseAssertion;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.rest.endpoint.LicenseEndpoint;
import org.jboss.pnc.rest.provider.LicenseProvider;
import org.jboss.pnc.rest.restmodel.LicenseRest;
import org.jboss.pnc.test.category.ContainerTest;
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
import static org.fest.assertions.Assertions.assertThat;
import static org.jboss.pnc.integration.Utils.JsonUtils.fromJson;
import static org.jboss.pnc.integration.Utils.JsonUtils.toJson;
import static org.jboss.pnc.integration.env.IntegrationTestEnv.getHttpPort;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class LicenseRestTest {

    private static final String LICENSE_REST_ENDPOINT = "/pnc-rest/rest/licenses/";
    private static final String LICENSE_REST_ENDPOINT_SPECIFIC = LICENSE_REST_ENDPOINT + "%d";

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static Integer licenseId;
    private static AuthenticationProvider authProvider;
    private static String access_token =  "no-auth";

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();

        WebArchive restWar = enterpriseArchive.getAsType(WebArchive.class, "/pnc-rest.war");
        restWar.addClass(LicenseProvider.class);
        restWar.addClass(LicenseEndpoint.class);
        restWar.addClass(LicenseRest.class);

        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @BeforeClass
    public static void setupAuth() throws IOException, ConfigurationParseException {
        if(AuthResource.authEnabled()) {
            Configuration configuration = new Configuration();
            AuthenticationModuleConfig config = configuration.getModuleConfig(AuthenticationModuleConfig.class);
            InputStream is = BuildRecordRestTest.class.getResourceAsStream("/keycloak.json");
            ExternalAuthentication ea = new ExternalAuthentication(is);
            authProvider = ea.authenticate(config.getUsername(), config.getPassword());
            access_token = authProvider.getTokenString();
        }
    }

    @Test
    @InSequence(0)
    public void shouldCreateNewLicense() throws Exception {
        //given
        String loremIpsumLicense = toJson(loremIpsumLicense());

        //when
        Response response = given()
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + access_token)
                .body(loremIpsumLicense).contentType(ContentType.JSON).port(getHttpPort())
                .header("Content-Type", "application/json; charset=UTF-8").when().post(LICENSE_REST_ENDPOINT);
        licenseId = ResponseUtils.getIdFromLocationHeader(response);

        //then
        ResponseAssertion.assertThat(response).hasStatus(201).hasLocationMatches(".*\\/pnc-rest\\/rest\\/licenses\\/\\d+");
        assertThat(licenseId).isNotNull();
    }

    @Test
    @InSequence(1)
    public void shouldUpdateLicense() throws Exception {
        //given
        LicenseRest loremIpsumLicenseModified = loremIpsumLicense();
        loremIpsumLicenseModified.setShortName("No-LI");
        loremIpsumLicenseModified.setFullContent("No Lorem Ipsum");

        //when
        Response putResponse = given()
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + access_token)
                .body(toJson(loremIpsumLicenseModified)).contentType(ContentType.JSON).port(getHttpPort()).when()
                .put(String.format(LICENSE_REST_ENDPOINT_SPECIFIC, licenseId));

        Response getResponse = given()
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + access_token)
                .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(LICENSE_REST_ENDPOINT_SPECIFIC, licenseId));

        LicenseRest noLoremIpsum = fromJson(getResponse.body().asString(), LicenseRest.class);

        //then
        ResponseAssertion.assertThat(putResponse).hasStatus(200);
        ResponseAssertion.assertThat(getResponse).hasStatus(200);
        assertThat(noLoremIpsum.getShortName()).isEqualTo("No-LI");
        assertThat(noLoremIpsum.getFullContent()).isEqualTo("No Lorem Ipsum");
    }

    @Test
    @InSequence(2)
    public void shouldDeleteLicense() throws Exception {
        //when
        Response deleteResponse = given()
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + access_token).port(getHttpPort()).when()
                .delete(String.format(LICENSE_REST_ENDPOINT_SPECIFIC, licenseId));

        Response getResponse = given()
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + access_token)
                .contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(LICENSE_REST_ENDPOINT_SPECIFIC, licenseId));

        //then
        ResponseAssertion.assertThat(deleteResponse).hasStatus(200);
        ResponseAssertion.assertThat(getResponse).hasStatus(204);
    }

    private LicenseRest loremIpsumLicense() {
        LicenseRest loremIpsumLicense = new LicenseRest();
        loremIpsumLicense.setFullContent("Lorem ipsum dolor sit amet, consectetur adipisicing elit");
        loremIpsumLicense.setFullName("Lorem Ipsum License");
        loremIpsumLicense.setRefUrl("http://lorem-ipsum.com");
        loremIpsumLicense.setShortName("LI");
        return loremIpsumLicense;
    }
}
