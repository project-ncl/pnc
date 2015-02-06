package org.jboss.pnc.integration;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.jboss.pnc.integration.env.IntegrationTestEnv.getHttpPort;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
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
import org.jboss.pnc.model.License;
import org.jboss.pnc.model.builder.LicenseBuilder;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.util.StringPropertyReplacer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

@RunWith(Arquillian.class)
public class RestTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static int productId;
    private static int productVersionId;
    private static int projectId;
    private static int configurationId;
    private static int userId;

    private static Integer newProductId;
    private static Integer licenseId;

    private static final String PRODUCT_REST_ENDPOINT = "/pnc-web/rest/product/";
    private static final String LICENSE_REST_ENDPOINT = "/pnc-web/rest/license/";
    private static final String LICENSE_REST_ENDPOINT_SPECIFIC = "/pnc-web/rest/license/%d";
    private static final String BUILD_CONFIGURATION_REST_ENDPOINT = "/pnc-web/rest/project/%d/configuration/%d";
    private static final String BUILD_CONFIGURATION_CLONE_REST_ENDPOINT = BUILD_CONFIGURATION_REST_ENDPOINT + "/clone/";

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
                .get(String.format(BUILD_CONFIGURATION_REST_ENDPOINT, projectId, configurationId)).then().statusCode(200)
                .body(containsJsonAttribute("id"));
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
            properties.put("_patchesUrl", "");
            properties.put("_creationTime", String.valueOf(1518382545038L));
            properties.put("_lastModificationTime", String.valueOf(155382545038L));
            properties.put("_repositories", "");

            rawJson = StringPropertyReplacer.replaceProperties(rawJson, properties);

            logger.info("New updated file:" + rawJson);

            given().body(rawJson).contentType(ContentType.JSON).port(getHttpPort()).when()
                    .put(String.format(BUILD_CONFIGURATION_REST_ENDPOINT, projectId, configurationId)).then().statusCode(200);

            // Reading updated resource
            Response response = given().contentType(ContentType.JSON).port(getHttpPort()).when()
                    .get(String.format(BUILD_CONFIGURATION_REST_ENDPOINT, projectId, configurationId));

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

        String buildConfigurationRestURI = String.format(BUILD_CONFIGURATION_CLONE_REST_ENDPOINT, projectId, configurationId);
        Response response = given().body("").contentType(ContentType.JSON).port(getHttpPort()).when()
                .post(buildConfigurationRestURI);
        Assertions.assertThat(response.statusCode()).isEqualTo(201);

        String location = response.getHeader("Location");
        logger.info("Found location in Response header: " + location);

        Integer clonedBuildConfigurationId = Integer.valueOf(location.substring(location.lastIndexOf(buildConfigurationRestURI)
                + buildConfigurationRestURI.length()));
        logger.info("Cloned id of buildConfiguration: " + clonedBuildConfigurationId);

        Response originalBuildConfiguration = given().contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(BUILD_CONFIGURATION_REST_ENDPOINT, projectId, configurationId));

        Response clonedBuildConfiguration = given().contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(BUILD_CONFIGURATION_REST_ENDPOINT, projectId, clonedBuildConfigurationId));

        Assertions.assertThat(originalBuildConfiguration.body().jsonPath().getInt("id")).isNotEqualTo(
                "_" + clonedBuildConfiguration.body().jsonPath().getInt("id"));
        Assertions.assertThat("_" + originalBuildConfiguration.body().jsonPath().getString("name")).isEqualTo(
                clonedBuildConfiguration.body().jsonPath().getString("name"));
        Assertions.assertThat(originalBuildConfiguration.body().jsonPath().getString("buildScript")).isEqualTo(
                clonedBuildConfiguration.body().jsonPath().getString("buildScript"));
        Assertions.assertThat(originalBuildConfiguration.body().jsonPath().getString("scmUrl")).isEqualTo(
                clonedBuildConfiguration.body().jsonPath().getString("scmUrl"));
        Assertions.assertThat(originalBuildConfiguration.body().jsonPath().getString("patchesUrl")).isEqualTo(
                clonedBuildConfiguration.body().jsonPath().getString("patchesUrl"));
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
                .delete(String.format(BUILD_CONFIGURATION_REST_ENDPOINT, projectId, configurationId)).then().statusCode(200);
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

    @Test
    @InSequence(15)
    public void shouldCreateNewProduct() {
        try {
            String rawJson = IoUtils.readFileOrResource("product", "product.json", getClass().getClassLoader());
            logger.info(rawJson);

            Response response = given().body(rawJson).contentType(ContentType.JSON).port(getHttpPort()).when()
                    .post("/pnc-web/rest/product/");
            Assertions.assertThat(response.statusCode()).isEqualTo(201);

            String location = response.getHeader("Location");
            logger.info("Found location in Response header: " + location);

            newProductId = Integer.valueOf(location.substring(location.lastIndexOf(PRODUCT_REST_ENDPOINT)
                    + PRODUCT_REST_ENDPOINT.length()));

            logger.info("Created id of product: " + newProductId);

        } catch (IOException e) {
            Assertions.fail("Could not read product.json file", e);
        }
    }

    @Test
    @InSequence(16)
    public void shouldUpdateProduct() {

        logger.info("### newProductId: " + newProductId);

        Response response = given().contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format("/pnc-web/rest/product/%d", newProductId));

        Assertions.assertThat(response.statusCode()).isEqualTo(200);
        Assertions.assertThat(response.body().jsonPath().getInt("id")).isEqualTo(newProductId);
        Assertions.assertThat(response.body().jsonPath().getString("name ")).isEqualTo(
                "JBoss Enterprise Application Platform 6");

        String rawJson = response.body().jsonPath().prettyPrint();
        rawJson = rawJson.replace("JBoss Enterprise Application Platform 6", "JBoss Enterprise Application Platform 7");

        logger.info("### rawJson: " + response.body().jsonPath().prettyPrint());

        given().body(rawJson).contentType(ContentType.JSON).port(getHttpPort()).when()
                .put(String.format("/pnc-web/rest/product/%d", newProductId)).then().statusCode(200);

        // Reading updated resource
        Response updateResponse = given().contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format("/pnc-web/rest/product/%d", newProductId));

        Assertions.assertThat(updateResponse.statusCode()).isEqualTo(200);
        Assertions.assertThat(updateResponse.body().jsonPath().getInt("id")).isEqualTo(newProductId);
        Assertions.assertThat(updateResponse.body().jsonPath().getString("name")).isEqualTo(
                "JBoss Enterprise Application Platform 7");

    }

    @Test
    @InSequence(17)
    public void shouldCreateNewLicense() {
        try {
            String gplLicense = IoUtils.readFileOrResource("license", "gpl_license.txt", getClass().getClassLoader());

            LicenseBuilder licenseBuilder = LicenseBuilder.newBuilder();
            licenseBuilder.fullName("GNU General Public License, version 2").refUrl("http://www.gnu.org/licenses/gpl-2.0.html")
                    .shortName("GPL").fullContent(gplLicense);

            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String json = ow.writeValueAsString(licenseBuilder.build());

            Response response = given().body(json).contentType(ContentType.JSON).port(getHttpPort())
                    .header("Content-Type", "application/json; charset=UTF-8").when().post(LICENSE_REST_ENDPOINT);
            Assertions.assertThat(response.statusCode()).isEqualTo(201);

            String location = response.getHeader("Location");
            logger.info("Found location in Response header: " + location);

            licenseId = Integer.valueOf(location.substring(location.lastIndexOf(LICENSE_REST_ENDPOINT)
                    + LICENSE_REST_ENDPOINT.length()));

            logger.info("Created id of license: " + licenseId);

        } catch (IOException e) {
            Assertions.fail("Could not read license.json file", e);
        }
    }

    @Test
    @InSequence(18)
    public void shouldUpdateLicense() {

        logger.info("### newLicenseId: " + licenseId);

        Response response = given().contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(LICENSE_REST_ENDPOINT_SPECIFIC, licenseId));

        Assertions.assertThat(response.statusCode()).isEqualTo(200);
        Assertions.assertThat(response.body().jsonPath().getInt("id")).isEqualTo(licenseId);
        Assertions.assertThat(response.body().jsonPath().getString("shortName")).isEqualTo("GPL");

        String rawJson = response.body().jsonPath().prettyPrint();
        rawJson = rawJson.replace("GPL", "GPL 2.0");

        logger.info("### rawJson: " + response.body().jsonPath().prettyPrint());

        given().body(rawJson).contentType(ContentType.JSON).port(getHttpPort()).when()
                .put(String.format(LICENSE_REST_ENDPOINT_SPECIFIC, licenseId)).then().statusCode(200);

        // Reading updated resource
        Response updateResponse = given().contentType(ContentType.JSON).port(getHttpPort()).when()
                .get(String.format(LICENSE_REST_ENDPOINT_SPECIFIC, licenseId));

        Assertions.assertThat(updateResponse.statusCode()).isEqualTo(200);
        Assertions.assertThat(updateResponse.body().jsonPath().getInt("id")).isEqualTo(licenseId);
        Assertions.assertThat(updateResponse.body().jsonPath().getString("shortName")).isEqualTo("GPL 2.0");

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
