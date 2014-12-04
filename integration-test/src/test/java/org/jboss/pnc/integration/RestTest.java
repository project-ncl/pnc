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

import static com.jayway.restassured.RestAssured.given;
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
        given().
        when().
            get("/pnc-web/rest/configuration").
        then().
            body(containsString("[{\"id\":2,\"identifier\":null,\"projectName\":\"demo\"}]"));
    }

    @Test
    public void shouldReturnSpecificConfiguration() {
        given().
        when().
            get("/pnc-web/rest/configuration/2").
        then().
            body(containsString("{\"id\":2,\"identifier\":null,\"projectName\":\"demo\"}"));
    }
}
