package org.jboss.pnc.integration;

import com.jayway.restassured.http.ContentType;
import org.hamcrest.CustomMatcher;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.jboss.pnc.integration.env.IntegrationTestEnv.getHttpPort;


@RunWith(Arquillian.class)
public class RestTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static int storedConfigurationId = -1;

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();
        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Test
    @InSequence(0)
    public void shouldInsertNewConfiguration() {
        String configurationJson = "{\"identifier\":\"pnc-1.0.0.DR1\",\"projectName\":\"PNC Project\",\"projectId\":1}";

        given().
                body(configurationJson).
                contentType(ContentType.JSON).
                port(getHttpPort()).
        when().
                post("/pnc-web/rest/configuration").
        then().
                statusCode(201).
                body(isANumber());
    }

    @Test
    @InSequence(1)
    public void shouldReturn400WhenInsertNewConfigurationWithoutProjectId() {
        String configurationJson = "{\"identifier\":\"test\",\"projectName\":\"PNC Project\"}";

        given().
                body(configurationJson).
                contentType(ContentType.JSON).
                port(getHttpPort()).
        when().
                post("/pnc-web/rest/configuration").
        then().
                statusCode(400);
    }

    @Test
    @InSequence(2)
    public void shouldReturnListOfConfigurations() {
        //given
        String allConfigurations = given()
                .port(getHttpPort()).get("/pnc-web/rest/configuration").asString();

        //when
        Integer notNullId = from(allConfigurations).get("[0].id");

        //then
        assertThat(notNullId).isNotNull();
    }

    @Test
    @InSequence(3)
    public void shouldReturnSpecificConfiguration() {
        given().
            port(getHttpPort()).
        when().
            get("/pnc-web/rest/configuration/" + storedConfigurationId).
                then().
            body(containsString("{\"id\":" + storedConfigurationId + ",\"identifier\":\"pnc-1.0.0.DR1\",\"projectName\":\"PNC Project\",\"projectId\":1}"));
    }

    @Test
    @InSequence(4)
    public void shouldUpdateExistingConfiguration() {
        String expectedJson = "{\"id\":" + storedConfigurationId + ",\"identifier\":\"test3\",\"projectName\":null,\"projectId\":1}";
        String configurationJson = "{\"id\":" + storedConfigurationId + ",\"identifier\":\"test3\"}";

        given().
                body(configurationJson).
                contentType(ContentType.JSON).
                port(getHttpPort()).
        when().
                put("/pnc-web/rest/configuration/" + storedConfigurationId).
        then().
                statusCode(200);

        //validate if it was really updated
        given().
                port(getHttpPort()).
                when().
                get("/pnc-web/rest/configuration/" + storedConfigurationId).
                then().
                body(containsString(expectedJson));
    }

    private CustomMatcher<String> isANumber() {
        return new CustomMatcher<String>("isANumber?") {
                @Override
                public boolean matches(Object o) {
                    try {
                        storedConfigurationId = Integer.parseInt(String.valueOf(o));
                        return true;
                    } catch (NumberFormatException nfe) {
                        return false;
                    }
                }
            };
    }
}
