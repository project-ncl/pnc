package org.jboss.pnc.integration;

import com.jayway.restassured.http.ContentType;
import groovy.json.StringEscapeUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.jboss.pnc.integration.env.IntegrationTestEnv.getHttpPort;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class NCL468Test {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Deployment(name="NCL468Test", testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();
        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Test
    public void shouldGetAllUsers() {
        final String toMatch = "[{\"id\":1,\"email\":\"demo-user@pnc.com\",\"firstName\":\"Demo First Name\",\"lastName\":\"Demo Last Name\",\"username\":\"demo-user\"}]";

        given().contentType(ContentType.JSON).port(getHttpPort()).when().get("/pnc-rest/rest/users")
            .then().assertThat().body(
                equalTo(StringEscapeUtils.unescapeJava(toMatch)));
    }
}
