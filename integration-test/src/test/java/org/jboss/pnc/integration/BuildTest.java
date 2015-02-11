package org.jboss.pnc.integration;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

import static org.jboss.pnc.integration.env.IntegrationTestEnv.getHttpPort;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;

@RunWith(Arquillian.class)
public class BuildTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();
        WebArchive war = enterpriseArchive.getAsType(WebArchive.class, "/pnc-web.war");
        war.addClass(BuildTest.class);
        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Test
    public void shouldTriggerBuildAndFinishWithoutProblems() {
        int configurationId = extractIdFromRest("/pnc-web/rest/configuration");

        given()
            .port(getHttpPort())
        .when()
            .post(String.format("/pnc-web/rest/configuration/%d/build", configurationId))
        .then()
            .statusCode(200);
    }

    Integer extractIdFromRest(String path) {
        String returnedObject = from(given()
                .port(getHttpPort()).get(path).asString()).get("[0].id").toString();
        return Integer.valueOf(returnedObject);
    }
}
