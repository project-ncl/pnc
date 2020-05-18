/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.pnc;

import io.restassured.http.Header;
import io.restassured.http.Headers;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.junit.BeforeClass;

import java.io.IOException;
import java.util.Base64;

public class AbstractTest {

    protected static final String BPM_NOTIFY_ENDPOINT = "/pnc-rest/bpm/tasks/{taskId}/notify";
    protected static final String PRODUCT_REST_ENDPOINT = "/pnc-rest/rest/products/";
    protected static final String PROJECT_REST_ENDPOINT = "/pnc-rest/rest/projects/";
    protected static final String PROJECT_SPECIFIC_REST_ENDPOINT = "/pnc-rest/rest/projects/%d";
    protected static final String PRODUCT_SPECIFIC_REST_ENDPOINT = "/pnc-rest/rest/products/%d";
    protected static final String CONFIGURATION_REST_ENDPOINT = "/pnc-rest/rest/build-configurations/";
    protected static final String CONFIGURATION_SPECIFIC_REST_ENDPOINT = "/pnc-rest/rest/build-configurations/%d";
    protected static final String REPOSITORY_CONFIGURATION_REST_ENDPOINT = "/pnc-rest/rest/repository-configurations/";
    protected static final String REPOSITORY_CONFIGURATION_SPECIFIC_REST_ENDPOINT = "/pnc-rest/rest/repository-configurations/%d";
    protected static final String REPOSITORY_CONFIGURATION_MATCH_REST_ENDPOINT = "/pnc-rest/rest/repository-configurations/match-by-scm-url";
    protected static final String ENVIRONMENTS_REST_ENDPOINT = "/pnc-rest/rest/environments";
    protected static final String SPECIFIC_ENVIRONMENT_REST_ENDPOINT = "/pnc-rest/rest/environments/%d";
    protected static final String PRODUCT_VERSION_REST_ENDPOINT = "/pnc-rest/rest/products/%d/product-versions/";
    protected static final String PRODUCT_MILESTONE_REST_ENDPOINT = "/pnc-rest/rest/product-milestones/";
    protected static final String PRODUCT_MILESTONE_PRODUCTVERSION_REST_ENDPOINT = "/pnc-rest/rest/product-milestones/product-versions/%d";
    protected static final String PRODUCT_MILESTONE_SPECIFIC_REST_ENDPOINT = PRODUCT_MILESTONE_REST_ENDPOINT + "%d";

    public static final String REST_WAR_PATH = "/rest.war";
    public static final String COORDINATOR_JAR = "/build-coordinator.jar";
    public static final String EXECUTOR_JAR = "/build-executor.jar";
    public static final String CAUSEWAY_CLIENT_JAR = "/causeway-client.jar";
    public static final String AUTH_JAR = "/auth.jar";
    public static final String BPM_JAR = "/bpm.jar";

    protected static final String FIRST_CONTENT_ID = "content[0].id";
    protected static final String CONTENT_ID = "content.id";
    protected static final String CONTENT_NAME = "content.name";

    protected static final Header acceptJsonHeader = new Header("Accept", "application/json");
    protected static Headers testHeaders;

    public static final String TEST_USER = "admin";
    public static final String TEST_PASS = "user.1234";

    @BeforeClass
    public static void setupAuth() throws IOException, ConfigurationParseException {
        Header authenticationHeader = getAuthenticationHeader();
        testHeaders = new Headers(authenticationHeader, acceptJsonHeader);
    }

    public static Header getAuthenticationHeader() {
        return new Header("Authorization", "Basic " + encodedCredentials());
    }

    public static String encodedCredentials() {
        return Base64.getEncoder().encodeToString((TEST_USER + ":" + TEST_PASS).getBytes());
    }

}
