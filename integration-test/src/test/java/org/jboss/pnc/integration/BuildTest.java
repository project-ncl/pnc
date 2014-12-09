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

import static com.jayway.restassured.RestAssured.get;
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

    //@Ignore //FIXME TEST fails with 500
    @Test
    public void shouldTriggerBuildAndFinishWithoutProblems() {
        Integer notNullId = from(get("/pnc-web/rest/configuration").asString()).get("[0].id");

        given()
        .when()
            .post("/pnc-web/rest/configuration/" + notNullId + "/build")
        .then()
            .statusCode(200);
    }
}
