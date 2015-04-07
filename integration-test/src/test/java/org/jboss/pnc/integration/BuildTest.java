package org.jboss.pnc.integration;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.auth.AuthenticationProvider;
import org.jboss.pnc.auth.ExternalAuthentication;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.pnc.test.category.RemoteTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;

import static org.jboss.pnc.integration.env.IntegrationTestEnv.getHttpPort;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;

@RunWith(Arquillian.class)
@Category({ContainerTest.class, RemoteTest.class})
public class BuildTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static AuthenticationProvider authProvider;


    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();
        WebArchive war = enterpriseArchive.getAsType(WebArchive.class, "/pnc-rest.war");
        war.addClass(BuildTest.class);
        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }
    
    @Before
    public void prepareData() {
        try {
            InputStream is = this.getClass().getResourceAsStream("/keycloak.json");
            ExternalAuthentication ea = new ExternalAuthentication(is);
            authProvider = ea.authenticate(System.getenv("PNC_EXT_OAUTH_USERNAME"), System.getenv("PNC_EXT_OAUTH_PASSWORD"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void shouldTriggerBuildAndFinishWithoutProblems() {
        int configurationId = extractIdFromRest("/pnc-rest/rest/configuration");

        given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    
            .port(getHttpPort())
        .when()
            .post(String.format("/pnc-rest/rest/configuration/%d/build", configurationId))
        .then()
            .statusCode(200);
    }

    Integer extractIdFromRest(String path) {
        String returnedObject = from(given().header("Accept", "application/json").header("Authorization", "Bearer " + authProvider.getTokenString())
                    
                .port(getHttpPort()).get(path).asString()).get("[0].id").toString();
        return Integer.valueOf(returnedObject);
    }
}
