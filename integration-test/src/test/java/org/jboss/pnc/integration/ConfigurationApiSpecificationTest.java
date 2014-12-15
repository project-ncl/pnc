package org.jboss.pnc.integration;


import com.jayway.restassured.http.ContentType;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.runtime.arquillian.ArquillianCucumber;
import cucumber.runtime.arquillian.api.Features;
import org.hamcrest.CustomMatcher;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.rest.restmodel.ProjectBuildConfigurationRest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

import static com.jayway.restassured.RestAssured.given;
import static org.jboss.pnc.integration.util.TestUtils.containsJsonAttribute;
import static org.jboss.pnc.integration.util.TestUtils.getHttpPort;
import static org.junit.Assert.assertEquals;

@RunWith(ArquillianCucumber.class)
@Features("/scenarios")
public class ConfigurationApiSpecificationTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static int productId;
    private static int productVersionId;
    private static int projectId;
    private static int lastHttpStatus;

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();
        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Given("^Standard list of Projects configurations$")
    public void standard_list_of_Projects_configurations() throws Throwable {
        given().port(getHttpPort()).get("/pnc-web/rest/product").then().body(containsJsonAttribute("[0].id", product -> productId = Integer.valueOf(product)));
        given().port(getHttpPort()).get(String.format("/pnc-web/rest/product/%d/version", productId)).then().body(containsJsonAttribute("[0].id", version -> productVersionId = Integer.valueOf(version)));
        given().port(getHttpPort()).get(String.format("/pnc-web/rest/product/%d/version/%d/project", productId, productVersionId)).then().body(containsJsonAttribute("[0].id", project -> projectId = Integer.valueOf(project)));
    }

    @When("^Adding a Configuration with (\\S+), (\\S+) and (\\S+)$")
    public void adding_Configuration_with_identifier_buildScript_and_scmUrl(String identifier, String buildScript, String scmUrl) throws Exception {
        ProjectBuildConfigurationRest configuration = new ProjectBuildConfigurationRest();
        if(!"'null'".equals(identifier)) {
            configuration.setIdentifier(identifier);
        }
        if(!"'null'".equals(buildScript)) {
            configuration.setBuildScript(buildScript);
        }
        if(!"'null'".equals(scmUrl)) {
            configuration.setScmUrl(scmUrl);
        }
        given()
                .port(getHttpPort())
                .contentType(ContentType.JSON)
                .body(configuration)
        .when()
                .post(String.format("/pnc-web/rest/product/%d/version/%d/project/%d/configuration", productId, productVersionId, projectId))
        .then()
                .statusCode(new CustomMatcher<Integer>("Custom Status Matcher") {
                    @Override
                    public boolean matches(Object o) {
                        lastHttpStatus = Integer.valueOf(o.toString());
                        return true;
                    }
                });
    }

    @Then("^API return status is (\\d+)$")
    public void api_return_status_is(int httpStatus) throws Exception {
        assertEquals(httpStatus, lastHttpStatus);
    }

}
