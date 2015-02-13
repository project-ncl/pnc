package org.jboss.pnc.integration;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.integration.Utils.ResponseUtils;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.integration.assertions.ResponseAssertion;
import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.model.OperationalSystem;
import org.jboss.pnc.rest.endpoint.EnvironmentEndpoint;
import org.jboss.pnc.rest.provider.EnvironmentProvider;
import org.jboss.pnc.rest.restmodel.EnvironmentRest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
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
public class EnvironmentRestTest {

    private static final String ENVIRONMENT_REST_ENDPOINT = "/pnc-web/rest/environment/";
    private static final String ENVIRONMENT_REST_ENDPOINT_SPECIFIC = ENVIRONMENT_REST_ENDPOINT + "%d";

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static Integer environmentId;

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();

        JavaArchive restJar = enterpriseArchive.getAsType(JavaArchive.class, "/pnc-rest.jar");
        restJar.addClass(EnvironmentProvider.class);
        restJar.addClass(EnvironmentEndpoint.class);
        restJar.addClass(EnvironmentRest.class);

        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Test
    @InSequence(0)
    public void shouldCreateNewEnvironment() throws Exception {
        //given
        String environment = toJson(exampleEnvironment());

        //when
        Response response = given().body(environment).contentType(ContentType.JSON).port(getHttpPort())
                .header("Content-Type", "application/json; charset=UTF-8").when().post(ENVIRONMENT_REST_ENDPOINT);
        environmentId = ResponseUtils.getIdFromLocationHeader(response);

        //then
        ResponseAssertion.assertThat(response).hasStatus(201).hasLocationMatches(".*\\/pnc-web\\/rest\\/environment\\/\\d+");
        assertThat(environmentId).isNotNull();
    }

    @Test
    @InSequence(1)
    public void shouldUpdateEnvironment() throws Exception {
        //given
        EnvironmentRest environmentModified = exampleEnvironment();
        environmentModified.setBuildType(BuildType.JAVA);
        environmentModified.setOperationalSystem(OperationalSystem.OSX);
        environmentModified.setId(environmentId);

        //when
        Response putResponse = given().body(toJson(environmentModified)).contentType(ContentType.JSON).port(getHttpPort()).when()
                .put(String.format(ENVIRONMENT_REST_ENDPOINT_SPECIFIC, environmentId));

        Response getResponse = given().contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(ENVIRONMENT_REST_ENDPOINT_SPECIFIC, environmentId));

        EnvironmentRest noLoremIpsum = fromJson(getResponse.body().asString(), EnvironmentRest.class);

        //then
        ResponseAssertion.assertThat(putResponse).hasStatus(200);
        ResponseAssertion.assertThat(getResponse).hasStatus(200);
        assertThat(noLoremIpsum.getBuildType()).isEqualTo(BuildType.JAVA);
        assertThat(noLoremIpsum.getOperationalSystem()).isEqualTo(OperationalSystem.OSX);
    }

    @Test
    @InSequence(2)
    public void shouldDeleteEnvironment() throws Exception {
        //when
        Response deleteResponse = given().port(getHttpPort()).when()
                .delete(String.format(ENVIRONMENT_REST_ENDPOINT_SPECIFIC, environmentId));

        Response getResponse = given().contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(ENVIRONMENT_REST_ENDPOINT_SPECIFIC, environmentId));

        //then
        ResponseAssertion.assertThat(deleteResponse).hasStatus(200);
        ResponseAssertion.assertThat(getResponse).hasStatus(204);
    }

    private EnvironmentRest exampleEnvironment() {
        EnvironmentRest environmentRest = new EnvironmentRest();
        environmentRest.setBuildType(BuildType.NATIVE);
        environmentRest.setOperationalSystem(OperationalSystem.LINUX);
        return environmentRest;
    }
}
