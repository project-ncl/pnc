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
package org.jboss.pnc.integration.client;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

import static com.jayway.restassured.RestAssured.given;
import static org.jboss.pnc.integration.env.IntegrationTestEnv.getHttpPort;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class RestClient {

    public Response get(String path, AbstractRestClient.QueryParam... queryParams) {
        RequestSpecification specification = request().when();
        if(queryParams != null && queryParams.length > 0) {
            for (AbstractRestClient.QueryParam qp : queryParams) {
                if(qp != null) {
                    specification.queryParam(qp.paramName, qp.paramValue);
                }
            }
        }

        return specification.get(path);
    }

    public Response post(String path) {
        return request().when().post(path);
    }

    public Response post(String path, Object body) {
        return request().when().body(body).post(path);
    }

    public Response put(String path, Object body) {
        return request().when().body(body).put(path);
    }

    public Response delete(String path) {
        return request().when().delete(path);
    }

    public RequestSpecification request() {
        return given()
                .auth().basic("admin", "user.1234")
                .log().all()
                .header("Accept", "application/json")
                .contentType(ContentType.JSON)
                .port(getHttpPort())
                .expect().log().all()
                .request();
    }

}
