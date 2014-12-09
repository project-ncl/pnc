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
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

@RunWith(Arquillian.class)
public class RestTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();
        WebArchive war = enterpriseArchive.getAsType(WebArchive.class, "/pnc-web.war");
        war.addClass(RestTest.class);
        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Test
    public void shouldReturnListOfConfigurations() {
        //given
        String allConfigurations = get("/pnc-web/rest/configuration").asString();

        //when
        Integer notNullId = from(allConfigurations).get("[0].id");

        //then
        assertThat(notNullId).isNotNull();
    }

    @Test
    public void shouldReturnSpecificConfiguration() {
        Integer notNullId = from(get("/pnc-web/rest/configuration").asString()).get("[0].id");

        given().
        when().
            get("/pnc-web/rest/configuration/" + notNullId).
        then().
            body(containsString("{\"id\":" + notNullId + ",\"identifier\":\"pnc-1.0.0.DR1\",\"projectName\":\"PNC Project\"}"));
    }
}
