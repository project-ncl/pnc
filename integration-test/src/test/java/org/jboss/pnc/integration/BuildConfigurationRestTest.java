package org.jboss.pnc.integration;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.common.util.IoUtils;
import org.jboss.pnc.integration.assertions.ResponseAssertion;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.integration.matchers.JsonMatcher;
import org.jboss.pnc.integration.template.JsonTemplateBuilder;
import org.jboss.pnc.rest.endpoint.BuildConfigurationEndpoint;
import org.jboss.pnc.rest.provider.BuildConfigurationProvider;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import static com.jayway.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.pnc.integration.env.IntegrationTestEnv.getHttpPort;

@RunWith(Arquillian.class)
public class BuildConfigurationRestTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String PRODUCT_REST_ENDPOINT = "/pnc-web/rest/product/";
    private static final String PRODUCT_VERSION_REST_ENDPOINT = "/pnc-web/rest/product/%d/version";
    private static final String PROJECT_PRODUCT_VERSION_REST_ENDPOINT = "/pnc-web/rest/project/product/%d/version/%d";
    private static final String CONFIGURATION_REST_ENDPOINT = "/pnc-web/rest/configuration/";
    private static final String CONFIGURATION_SPECIFIC_REST_ENDPOINT = "/pnc-web/rest/configuration/%d";
    private static final String CONFIGURATION_CLONE_REST_ENDPOINT = "/pnc-web/rest/configuration/%d/clone";

    private static int productId;
    private static int productVersionId;
    private static int projectId;
    private static int configurationId;

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();

        JavaArchive restJar = enterpriseArchive.getAsType(JavaArchive.class, "/pnc-rest.jar");
        restJar.addClass(BuildConfigurationProvider.class);
        restJar.addClass(BuildConfigurationEndpoint.class);
        restJar.addClass(BuildConfigurationRest.class);

        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Test
    @InSequence(-4)
    public void prepareProductId() {
        given().contentType(ContentType.JSON).port(getHttpPort()).when().get(PRODUCT_REST_ENDPOINT).then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("[0].id", value -> productId = Integer.valueOf(value)));
    }

    @Test
    @InSequence(-3)
    public void prepareProductVersionId() {
        given().contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(PRODUCT_VERSION_REST_ENDPOINT, productId)).then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("[0].id", value -> productVersionId = Integer.valueOf(value)));
    }

    @Test
    @InSequence(-2)
    public void prepareProjectId() {
        given().contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(PROJECT_PRODUCT_VERSION_REST_ENDPOINT, productId, productVersionId)).then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("[0].id", value -> projectId = Integer.valueOf(value)));
    }

    @Test
    @InSequence(-1)
    public void shouldGetAllBuildConfigurations() {
        given().contentType(ContentType.JSON).port(getHttpPort()).when().get(CONFIGURATION_REST_ENDPOINT).then()
                .statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("[0].id", value -> configurationId = Integer.valueOf(value)));
    }

    @Test
    public void shouldGetSpecificBuildConfiguration() {
        given().contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(CONFIGURATION_SPECIFIC_REST_ENDPOINT, configurationId)).then().statusCode(200)
                .body(JsonMatcher.containsJsonAttribute("id"));
    }

    @Test
    public void shouldCreateNewBuildConfiguration() throws IOException {
        String rawJson = loadJsonFromFile("buildConfiguration");

        given().body(rawJson).contentType(ContentType.JSON).port(getHttpPort()).when().post(CONFIGURATION_REST_ENDPOINT).then()
                .statusCode(201);
    }

    @Test
    public void shouldUpdateBuildConfiguration() throws IOException {
        // given
        final String updatedScmUrl = "https://github.com/project-ncl/pnc.git";
        final String updatedBuildScript = "mvn clean deploy -Dmaven.test.skip=true";
        final String updatedName = "pnc-1.0.1.ER1";
        final String updatedProjectId = String.valueOf(projectId);

        JsonTemplateBuilder configurationTemplate = JsonTemplateBuilder.fromResource("buildConfiguration_template");
        configurationTemplate.addValue("_id", String.valueOf(configurationId));
        configurationTemplate.addValue("_name", updatedName);
        configurationTemplate.addValue("_buildScript", updatedBuildScript);
        configurationTemplate.addValue("_scmRepoURL", updatedScmUrl);
        configurationTemplate.addValue("_patchesUrl", "");
        configurationTemplate.addValue("_creationTime", String.valueOf(1518382545038L));
        configurationTemplate.addValue("_lastModificationTime", String.valueOf(155382545038L));
        configurationTemplate.addValue("_repositories", "");
        configurationTemplate.addValue("_projectId", updatedProjectId);

        // when
        given().body(configurationTemplate.fillTemplate()).contentType(ContentType.JSON).port(getHttpPort()).when()
                .put(String.format(CONFIGURATION_SPECIFIC_REST_ENDPOINT, configurationId)).then().statusCode(200);

        Response response = given().contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(CONFIGURATION_SPECIFIC_REST_ENDPOINT, configurationId));

        // then
        ResponseAssertion.assertThat(response).hasStatus(200);
        ResponseAssertion.assertThat(response).hasJsonValueEqual("id", configurationId).hasJsonValueEqual("name", updatedName)
                .hasJsonValueEqual("buildScript", updatedBuildScript).hasJsonValueEqual("scmRepoURL", updatedScmUrl)
                .hasJsonValueEqual("projectId", updatedProjectId);
    }

    @Test
    public void shouldCloneBuildConfiguration() {
        String buildConfigurationRestURI = String.format(CONFIGURATION_CLONE_REST_ENDPOINT, configurationId);
        Response response = given().body("").contentType(ContentType.JSON).port(getHttpPort()).when()
                .post(buildConfigurationRestURI);

        String location = response.getHeader("Location");
        Integer clonedBuildConfigurationId = Integer.valueOf(location.substring(location.lastIndexOf("/") + 1));
        logger.info("Cloned id of buildConfiguration: " + clonedBuildConfigurationId);

        Response originalBuildConfiguration = given().contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(CONFIGURATION_SPECIFIC_REST_ENDPOINT, configurationId));

        Response clonedBuildConfiguration = given().contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(CONFIGURATION_SPECIFIC_REST_ENDPOINT, clonedBuildConfigurationId));

        ResponseAssertion.assertThat(response).hasStatus(201).hasLocationMatches(".*\\/pnc-web\\/rest\\/configuration\\/\\d+");

        assertThat(originalBuildConfiguration.body().jsonPath().getString("creationTime")).isNotEqualTo(
                clonedBuildConfiguration.body().jsonPath().getString("creationTime"));
        assertThat(originalBuildConfiguration.body().jsonPath().getInt("id")).isNotEqualTo(
                "_" + clonedBuildConfiguration.body().jsonPath().getInt("id"));

        assertThat("_" + originalBuildConfiguration.body().jsonPath().getString("name")).isEqualTo(
                clonedBuildConfiguration.body().jsonPath().getString("name"));
        assertThat(originalBuildConfiguration.body().jsonPath().getString("buildScript")).isEqualTo(
                clonedBuildConfiguration.body().jsonPath().getString("buildScript"));
        assertThat(originalBuildConfiguration.body().jsonPath().getString("scmRepoURL")).isEqualTo(
                clonedBuildConfiguration.body().jsonPath().getString("scmRepoURL"));
        assertThat(originalBuildConfiguration.body().jsonPath().getString("patchesUrl")).isEqualTo(
                clonedBuildConfiguration.body().jsonPath().getString("patchesUrl"));
        assertThat(originalBuildConfiguration.body().jsonPath().getString("lastModificationTime")).isEqualTo(
                clonedBuildConfiguration.body().jsonPath().getString("lastModificationTime"));
        assertThat(originalBuildConfiguration.body().jsonPath().getString("repositories")).isEqualTo(
                clonedBuildConfiguration.body().jsonPath().getString("repositories"));
    }

//    @Test
//    @InSequence(999)
//    public void shouldDeleteProjectConfiguration() {
//        given().contentType(ContentType.JSON).port(getHttpPort()).when()
//                .delete(String.format(CONFIGURATION_SPECIFIC_REST_ENDPOINT, configurationId)).then().statusCode(200);
//    }

    private String loadJsonFromFile(String resource) throws IOException {
        return IoUtils.readFileOrResource(resource, resource + ".json", getClass().getClassLoader());
    }
}
