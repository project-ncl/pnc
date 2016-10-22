/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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

import com.jayway.restassured.response.Header;
import com.jayway.restassured.response.Headers;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.junit.BeforeClass;

import java.io.IOException;

public class AbstractTest {

    protected static final String BPM_NOTIFY_ENDPOINT = "/pnc-rest/bpm/tasks/{taskId}/notify";
    protected static final String PRODUCT_REST_ENDPOINT = "/pnc-rest/rest/products/";
    protected static final String PROJECT_REST_ENDPOINT = "/pnc-rest/rest/projects/";
    protected static final String PROJECT_SPECIFIC_REST_ENDPOINT = "/pnc-rest/rest/projects/%d";
    protected static final String PRODUCT_SPECIFIC_REST_ENDPOINT = "/pnc-rest/rest/products/%d";
    protected static final String CONFIGURATION_REST_ENDPOINT = "/pnc-rest/rest/build-configurations/";
    protected static final String CONFIGURATION_SPECIFIC_REST_ENDPOINT = "/pnc-rest/rest/build-configurations/%d";
    protected static final String SPECIFIC_ENVIRONMENT_REST_ENDPOINT = "/pnc-rest/rest/environments/%d";
    protected static final String PRODUCT_VERSION_REST_ENDPOINT = "/pnc-rest/rest/products/%d/product-versions/";
    protected static final String PRODUCT_MILESTONE_REST_ENDPOINT = "/pnc-rest/rest/product-milestones/";
    protected static final String PRODUCT_MILESTONE_PRODUCTVERSION_REST_ENDPOINT = "/pnc-rest/rest/product-milestones/product-versions/%d";
    protected static final String PRODUCT_MILESTONE_SPECIFIC_REST_ENDPOINT = PRODUCT_MILESTONE_REST_ENDPOINT + "%d";

    public static final String REST_WAR_PATH = "/rest.war";
    protected static final String FIRST_CONTENT_ID = "content[0].id";
    protected static final String CONTENT_ID = "content.id";
    protected static final String CONTENT_NAME = "content.name";

    protected static final Header acceptJsonHeader = new Header("Accept", "application/json");
    protected static Headers testHeaders;

    @BeforeClass
    public static void setupAuth() throws IOException, ConfigurationParseException {
        testHeaders = new Headers(acceptJsonHeader);
    }
}
