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
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.jboss.pnc.integration.env.IntegrationTestEnv.getHttpPort;


@RunWith(Arquillian.class)
public class RestTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static int productId;
    private static int productVersionId;
    private static int projectId;
    private static int configurationId;

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();
        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Test
    @InSequence(0)
    public void shouldGetAllProducts() {
        given().
                contentType(ContentType.JSON).
                port(getHttpPort()).
        when().
                get("/pnc-web/rest/product").
        then().
                statusCode(200).
                body(containsJsonAttribute("[0].id", value -> productId = Integer.valueOf(value)));
    }

    @Test
    @InSequence(1)
    public void shouldGetSpecificProduct() {
        given().
                contentType(ContentType.JSON).
                port(getHttpPort()).
        when().
                get(String.format("/pnc-web/rest/product/%d", productId)).
        then().
                statusCode(200).
                body(containsJsonAttribute("id"));
    }

    @Test
    @InSequence(2)
    public void shouldGetAllProductsVersions() {
        given().
                contentType(ContentType.JSON).
                port(getHttpPort()).
        when().
                get(String.format("/pnc-web/rest/product/%d/version", productId)).
        then().
                statusCode(200).
                body(containsJsonAttribute("[0].id", value -> productVersionId = Integer.valueOf(value)));
    }

    @Test
    @InSequence(3)
    public void shouldSpecificProductsVersions() {
        given().
                contentType(ContentType.JSON).
                port(getHttpPort()).
        when().
                get(String.format("/pnc-web/rest/product/%d/version/%d", productId, productVersionId)).
        then().
                statusCode(200).
                body(containsJsonAttribute("id"));
    }

    @Test
    @InSequence(4)
    public void shouldGetAllProjectAssignedToProductAndProductVersion() {
        given().
                contentType(ContentType.JSON).
                port(getHttpPort()).
                when().
                get(String.format("/pnc-web/rest/product/%d/version/%d/project", productId, productVersionId)).
                then().
                statusCode(200).
                body(containsJsonAttribute("[0].id", value -> projectId = Integer.valueOf(value)));
    }

    @Test
    @InSequence(5)
    public void shouldSpecificProjectAssignedToProductAndProductVersion() {
        given().
                contentType(ContentType.JSON).
                port(getHttpPort()).
        when().
                get(String.format("/pnc-web/rest/product/%d/version/%d/project/%d", productId, productVersionId, projectId)).
        then().
                statusCode(200).
                body(containsJsonAttribute("id"));
    }

    @Test
    @InSequence(6)
    public void shouldGetAllProjectConfigurationsAssignedToTheProject() {
        given().
                contentType(ContentType.JSON).
                port(getHttpPort()).
        when().
                get(String.format("/pnc-web/rest/product/%d/version/%d/project/%d/configuration", productId, productVersionId, projectId)).
        then().
                statusCode(200).
                body(containsJsonAttribute("[0].id", value -> configurationId = Integer.valueOf(value)));
    }

    @Test
    @InSequence(7)
    public void shouldSpecificProjectConfigurationsAssignedToTheProject() {
        given().
                contentType(ContentType.JSON).
                port(getHttpPort()).
        when().
                get(String.format("/pnc-web/rest/product/%d/version/%d/project/%d/configuration/%d", productId, productVersionId, projectId, configurationId)).
        then().
                statusCode(200).
                body(containsJsonAttribute("id"));
    }

    @Test
    @InSequence(8)
    public void shouldCreateNewProjectConfiguration() {
        String rawJson = "{\n" +
                "                \"identifier\": \"pnc-1.0.0.DR1\",\n" +
                "                \"buildScript\": \"mvn clean deploy -Dmaven.test.skip\",\n" +
                "                \"scmUrl\": \"https://github.com/project-ncl/pnc.git\",\n" +
                "                \"patchesUrl\": null,\n" +
                "                \"creationTime\": 1418382545021,\n" +
                "                \"lastModificationTime\": 1418382545038,\n" +
                "                \"repositories\": null\n" +
                "        }";

        given().
                body(rawJson).
                contentType(ContentType.JSON).
                port(getHttpPort()).
        when().
                post(String.format("/pnc-web/rest/product/%d/version/%d/project/%d/configuration/", productId, productVersionId, projectId)).
        then().
                statusCode(201);
    }

    @Test
    @InSequence(9)
    public void shouldDeleteProjectConfiguration() {
        given().
                contentType(ContentType.JSON).
                port(getHttpPort()).
        when().
                delete(String.format("/pnc-web/rest/product/%d/version/%d/project/%d/configuration/%d", productId, productVersionId, projectId, configurationId)).
        then().
                statusCode(200);
    }

    private CustomMatcher<String> containsJsonAttribute(String jsonAttribute, Consumer<String>... actionWhenMatches) {
        return new CustomMatcher<String>("matchesJson") {
            @Override
            public boolean matches(Object o) {
                String rawJson = String.valueOf(o).intern();
                logger.debug("Evaluating raw JSON: " + rawJson);
                Object value = from(rawJson).get(jsonAttribute);
                logger.debug("Got value from JSon: " + value);
                if(value != null) {
                    if(actionWhenMatches != null) {
                        Stream.of(actionWhenMatches).forEach(action -> action.accept(String.valueOf(value)));
                    }
                    return true;
                }
                return false;
            }
        };
    }
}
