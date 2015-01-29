package org.jboss.pnc.integration;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.hamcrest.Matchers.equalTo;
import static org.jboss.pnc.integration.env.IntegrationTestEnv.getHttpPort;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.hamcrest.CustomMatcher;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.common.util.IoUtils;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.util.StringPropertyReplacer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.response.ResponseBody;
import com.jayway.restassured.response.ResponseBodyData;

@RunWith(Arquillian.class)
public class RestTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static int productId;
    private static int productVersionId;
    private static int projectId;
    private static int configurationId;
    private static int userId;

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();
        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Test
    @InSequence(0)
    public void shouldGetAllProducts() {
        given().contentType(ContentType.JSON).port(getHttpPort()).when().get("/pnc-web/rest/product").then().statusCode(200)
                .body(containsJsonAttribute("[0].id", value -> productId = Integer.valueOf(value)));
    }

    @Test
    @InSequence(1)
    public void shouldGetSpecificProduct() {
        given().contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format("/pnc-web/rest/product/%d", productId)).then().statusCode(200)
                .body(containsJsonAttribute("id"));
    }

    @Test
    @InSequence(2)
    public void shouldGetAllProductsVersions() {
        given().contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format("/pnc-web/rest/product/%d/version", productId)).then().statusCode(200)
                .body(containsJsonAttribute("[0].id", value -> productVersionId = Integer.valueOf(value)));
    }

    @Test
    @InSequence(3)
    public void shouldSpecificProductsVersions() {
        given().contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format("/pnc-web/rest/product/%d/version/%d", productId, productVersionId)).then().statusCode(200)
                .body(containsJsonAttribute("id"));
    }

    @Test
    @InSequence(4)
    public void shouldGetAllProjectAssignedToProductAndProductVersion() {
        given().contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format("/pnc-web/rest/product/%d/version/%d/project", productId, productVersionId)).then()
                .statusCode(200).body(containsJsonAttribute("[0].id", value -> projectId = Integer.valueOf(value)));
    }

    @Test
    @InSequence(5)
    public void shouldGetSpecificProject() {
        given().contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format("/pnc-web/rest/project/%d", projectId)).then().statusCode(200)
                .body(containsJsonAttribute("id"));
    }

    @Test
    @InSequence(6)
    public void shouldGetAllBuildConfigurationsAssignedToTheProject() {
        given().contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format("/pnc-web/rest/project/%d/configuration", projectId)).then().statusCode(200)
                .body(containsJsonAttribute("[0].id", value -> configurationId = Integer.valueOf(value)));
    }

    @Test
    @InSequence(7)
    public void shouldGetSpecificBuildConfigurationAssignedToProject() {
        given().contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format("/pnc-web/rest/project/%d/configuration/%d", projectId, configurationId)).then()
                .statusCode(200).body(containsJsonAttribute("id"));
    }

    @Test
    @InSequence(8)
    public void shouldCreateNewBuildConfiguration() {
        try {
            String rawJson = IoUtils.readFileOrResource("buildConfiguration", "buildConfiguration.json", getClass()
                    .getClassLoader());
            logger.info(rawJson);

            given().body(rawJson).contentType(ContentType.JSON).port(getHttpPort()).when()
                    .post(String.format("/pnc-web/rest/project/%d/configuration/", projectId)).then().statusCode(201);

        } catch (IOException e) {
            Assertions.fail("Could not read buildConfiguration.json file", e);
        }
    }

    @Test
    @InSequence(9)
    public void shouldUpdateBuildConfiguration() {

        try {
            String rawJson = IoUtils.readFileOrResource("buildConfiguration_template", "buildConfiguration_template.json",
                    getClass().getClassLoader());
            logger.info(rawJson);

            final String _SCMURL = "https://github.com/project-ncl/pnc.git";
            final String _BUILDSCRIPT = "mvn clean deploy -Dmaven.test.skip=true";
            final String _NAME = "pnc-1.0.1.ER1";

            Properties properties = new Properties();
            properties.put("_id", String.valueOf(configurationId));
            properties.put("_name", _NAME);
            properties.put("_buildScript", _BUILDSCRIPT);
            properties.put("_scmUrl", _SCMURL);
            properties.put("_creationTime", String.valueOf(1518382545038L));
            properties.put("_lastModificationTime", String.valueOf(155382545038L));
            properties.put("_repositories", "");

            rawJson = StringPropertyReplacer.replaceProperties(rawJson, properties);

            logger.info("New updated file:" + rawJson);

            given().body(rawJson).contentType(ContentType.JSON).port(getHttpPort()).when()
                    .put(String.format("/pnc-web/rest/project/%d/configuration/%d", projectId, configurationId)).then()
                    .statusCode(200);

            // Reading updated resource
            Response response = given().contentType(ContentType.JSON).port(getHttpPort()).when()
                    .get(String.format("/pnc-web/rest/project/%d/configuration/%d", projectId, configurationId));

            Assertions.assertThat(response.statusCode()).isEqualTo(200);
            Assertions.assertThat(response.body().jsonPath().getInt("id")).isEqualTo(configurationId);
            Assertions.assertThat(response.body().jsonPath().getString("name")).isEqualTo(_NAME);
            Assertions.assertThat(response.body().jsonPath().getString("buildScript")).isEqualTo(_BUILDSCRIPT);
            Assertions.assertThat(response.body().jsonPath().getString("scmUrl")).isEqualTo(_SCMURL);

        } catch (IOException e) {
            Assertions.fail("Could not read buildConfiguration_template.json file", e);
        }
    }

    @Test
    @InSequence(10)
    public void shouldCloneBuildConfiguration() {

        Response response = given().body("").contentType(ContentType.JSON).port(getHttpPort()).when()
                .post(String.format("/pnc-web/rest/project/%d/configuration/%d/clone", projectId, configurationId));
        Assertions.assertThat(response.statusCode()).isEqualTo(201);

        String location = response.getHeader("Location");
        logger.info("Found location in Response header: " + location);

        int lastIndexOf = location.lastIndexOf(String.format("/pnc-web/rest/project/%d/configuration/%d/clone/", projectId,
                configurationId));

        String clonedResourceURI = location.substring(lastIndexOf);
        logger.info("ClonedResourceURI: " + clonedResourceURI);

        String clonedBuildConfigurationId = clonedResourceURI.replace(
                String.format("/pnc-web/rest/project/%d/configuration/%d/clone/", projectId, configurationId), "");
        logger.info("Cloned id of buildConfiguration: " + clonedBuildConfigurationId);

        Response originalBuildConfiguration = given().contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format("/pnc-web/rest/project/%d/configuration/%d", projectId, configurationId));

        Response clonedBuildConfiguration = given()
                .contentType(ContentType.JSON)
                .port(getHttpPort())
                .when()
                .get(String.format("/pnc-web/rest/project/%d/configuration/%d", projectId,
                        Integer.valueOf(clonedBuildConfigurationId)));

        Assertions.assertThat(originalBuildConfiguration.body().jsonPath().getInt("id")).isNotEqualTo(
                "_" + clonedBuildConfiguration.body().jsonPath().getInt("id"));
        Assertions.assertThat("_" + originalBuildConfiguration.body().jsonPath().getString("name")).isEqualTo(
                clonedBuildConfiguration.body().jsonPath().getString("name"));
        Assertions.assertThat(originalBuildConfiguration.body().jsonPath().getString("buildScript")).isEqualTo(
                clonedBuildConfiguration.body().jsonPath().getString("buildScript"));
        Assertions.assertThat(originalBuildConfiguration.body().jsonPath().getString("scmUrl")).isEqualTo(
                clonedBuildConfiguration.body().jsonPath().getString("scmUrl"));
        Assertions.assertThat(originalBuildConfiguration.body().jsonPath().getString("creationTime")).isEqualTo(
                clonedBuildConfiguration.body().jsonPath().getString("creationTime"));
        Assertions.assertThat(originalBuildConfiguration.body().jsonPath().getString("lastModificationTime")).isEqualTo(
                clonedBuildConfiguration.body().jsonPath().getString("lastModificationTime"));
        Assertions.assertThat(originalBuildConfiguration.body().jsonPath().getString("repositories")).isEqualTo(
                clonedBuildConfiguration.body().jsonPath().getString("repositories"));

    }

    @Test
    @InSequence(11)
    public void shouldDeleteProjectConfiguration() {
        given().contentType(ContentType.JSON).port(getHttpPort()).when()
                .delete(String.format("/pnc-web/rest/project/%d/configuration/%d", projectId, configurationId)).then()
                .statusCode(200);
    }

    @Test
    @InSequence(12)
    public void shouldGetAllUsers() {
        given().contentType(ContentType.JSON).port(getHttpPort()).when().get("/pnc-web/rest/user").then().statusCode(200)
                .body(containsJsonAttribute("[0].id", value -> userId = Integer.valueOf(value)));
    }

    @Test
    @InSequence(13)
    public void shouldGetSpecificUser() {
        given().contentType(ContentType.JSON).port(getHttpPort()).when().get(String.format("/pnc-web/rest/user/%d", userId))
                .then().statusCode(200).body(containsJsonAttribute("id"));
    }

    @Test
    @InSequence(14)
    public void shouldCreateNewUser() {
        try {
            String rawJson = IoUtils.readFileOrResource("user", "user.json", getClass().getClassLoader());
            logger.info(rawJson);
            given().body(rawJson).contentType(ContentType.JSON).port(getHttpPort()).when().post("/pnc-web/rest/user/").then()
                    .statusCode(201);

        } catch (IOException e) {
            Assertions.fail("Could not read user.json file", e);
        }
    }

    private CustomMatcher<String> containsJsonAttribute(String jsonAttribute, Consumer<String>... actionWhenMatches) {
        return new CustomMatcher<String>("matchesJson") {
            @Override
            public boolean matches(Object o) {
                String rawJson = String.valueOf(o).intern();
                logger.debug("Evaluating raw JSON: " + rawJson);
                Object value = from(rawJson).get(jsonAttribute);
                logger.debug("Got value from JSon: " + value);
                if (value != null) {
                    if (actionWhenMatches != null) {
                        Stream.of(actionWhenMatches).forEach(action -> action.accept(String.valueOf(value)));
                    }
                    return true;
                }
                return false;
            }
        };
    }
}
