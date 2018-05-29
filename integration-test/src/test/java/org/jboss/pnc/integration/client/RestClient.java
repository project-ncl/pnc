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

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class RestClient {

    private final ConnectionInfo connectionInfo;

    public RestClient(ConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    public Response get(String path, AbstractRestClient.QueryParam... queryParams) {
        RequestSpecification specification = request().when();
        if(queryParams != null && queryParams.length > 0) {
            for (AbstractRestClient.QueryParam qp : queryParams) {
                if(qp != null) {
                    specification.queryParam(qp.paramName, qp.paramValue);
                }
            }
        }
        return specification.get(addHost(path));
    }

    public Response post(String path) {
        return request().when().post(addHost(path));
    }

    public Response post(String path, Object body) {
        return request().when().body(body).post(addHost(path));
    }

    public Response put(String path, Object body) {
        return request().when().body(body).put(addHost(path));
    }

    public Response delete(String path) {
        return request().when().delete(addHost(path));
    }

    public String addHost(String path) {
        if (connectionInfo.getHost() != null) {
            return "http://" + connectionInfo.getHost() + path;
        } else {
            return path;
        }
    }

    public RequestSpecification request() {
        RequestSpecification requestSpec = given()
                .header("Accept", "application/json")
                .contentType(ContentType.JSON)
                .port(connectionInfo.getPort());
        ConnectionInfo.BasicAuth basicAuth = connectionInfo.getBasicAuth();
        if (basicAuth != null) {
            requestSpec.auth().basic(basicAuth.getUsername(), basicAuth.getPassword());
        }
        String bearerToken = connectionInfo.getBearerToken();
        if (bearerToken != null) {
            requestSpec.header("Authorization", "Bearer " + bearerToken);
        }

        requestSpec
            .log().all()
            .expect().log().all();

        return requestSpec.request();
    }
}
