package org.jboss.pnc.integration;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.integration.Utils.ResponseUtils;
import org.jboss.pnc.integration.assertions.ResponseAssertion;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.rest.endpoint.LicenseEndpoint;
import org.jboss.pnc.rest.provider.LicenseProvider;
import org.jboss.pnc.rest.restmodel.LicenseRest;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

import static com.jayway.restassured.RestAssured.given;
import static org.fest.assertions.Assertions.assertThat;
import static org.jboss.pnc.integration.Utils.JsonUtils.fromJson;
import static org.jboss.pnc.integration.Utils.JsonUtils.toJson;
import static org.jboss.pnc.integration.env.IntegrationTestEnv.getHttpPort;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class LicenseRestTest {

    private static final String LICENSE_REST_ENDPOINT = "/pnc-rest/rest/license/";
    private static final String LICENSE_REST_ENDPOINT_SPECIFIC = LICENSE_REST_ENDPOINT + "%d";

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static Integer licenseId;

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

    @Test
    @InSequence(0)
    public void shouldCreateNewLicense() throws Exception {
        //given
        String loremIpsumLicense = toJson(loremIpsumLicense());

        //when
        Response response = given().body(loremIpsumLicense).contentType(ContentType.JSON).port(getHttpPort())
                .header("Content-Type", "application/json; charset=UTF-8").when().post(LICENSE_REST_ENDPOINT);
        licenseId = ResponseUtils.getIdFromLocationHeader(response);

        //then
        ResponseAssertion.assertThat(response).hasStatus(201).hasLocationMatches(".*\\/pnc-rest\\/rest\\/license\\/\\d+");
        assertThat(licenseId).isNotNull();
    }

    @Test
    @InSequence(1)
    public void shouldUpdateLicense() throws Exception {
        //given
        LicenseRest loremIpsumLicenseModified = loremIpsumLicense();
        loremIpsumLicenseModified.setShortName("No-LI");
        loremIpsumLicenseModified.setFullContent("No Lorem Ipsum");
        loremIpsumLicenseModified.setId(licenseId);

        //when
        Response putResponse = given().body(toJson(loremIpsumLicenseModified)).contentType(ContentType.JSON).port(getHttpPort()).when()
                .put(String.format(LICENSE_REST_ENDPOINT_SPECIFIC, licenseId));

        Response getResponse = given().contentType(ContentType.JSON).port(getHttpPort()).when()
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
        Response deleteResponse = given().port(getHttpPort()).when()
                .delete(String.format(LICENSE_REST_ENDPOINT_SPECIFIC, licenseId));

        Response getResponse = given().contentType(ContentType.JSON).port(getHttpPort()).when()
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
