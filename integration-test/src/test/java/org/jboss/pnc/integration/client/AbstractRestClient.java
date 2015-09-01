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
import org.jboss.pnc.auth.AuthenticationProvider;
import org.jboss.pnc.auth.ExternalAuthentication;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.AuthenticationModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.integration.client.util.RestResponse;
import org.jboss.pnc.integration.utils.AuthResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;

import static com.jayway.restassured.RestAssured.given;
import static org.jboss.pnc.integration.env.IntegrationTestEnv.getHttpPort;

public abstract class AbstractRestClient<T> {

    protected Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    protected AuthenticationProvider authProvider;
    protected String access_token = "no-auth";
    protected boolean authInitialized = false;

    protected String collectionUrl;

    protected Class<T> entityClass;

    protected AbstractRestClient(String collectionUrl, Class<T> entityClass) {
        this.entityClass = entityClass;

        if(collectionUrl.endsWith("/")) {
            this.collectionUrl = collectionUrl;
        } else {
            this.collectionUrl = collectionUrl + "/";
        }

        try {
            initAuth();
        } catch (IOException | ConfigurationParseException e) {
            throw new AssertionError("Error while initializing auth", e);
        }
    }

    protected void initAuth() throws IOException, ConfigurationParseException {
        if (AuthResource.authEnabled() && !authInitialized) {
            AuthenticationModuleConfig config = new Configuration().getModuleConfig(new PncConfigProvider<>(AuthenticationModuleConfig.class));
            InputStream is = AbstractRestClient.class.getResourceAsStream("/keycloak.json");
            ExternalAuthentication ea = new ExternalAuthentication(is);
            authProvider = ea.authenticate(config.getUsername(), config.getPassword());
            access_token = authProvider.getTokenString();
            authInitialized = true;
        }
    }

    protected Response post(String path, Object body) {
        return request().when().body(body).post(path);
    }

    protected Response put(String path, Object body) {
        return request().when().body(body).put(path);
    }

    protected Response delete(String path) {
        return request().when().delete(path);
    }

    protected Response get(String path) {
        return request().when().get(path);
    }

    protected RequestSpecification request() {
        return given()
                .log().all()
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + access_token)
                .contentType(ContentType.JSON)
                .port(getHttpPort())
                    .expect().log().all()
                .request();
    }

    protected Integer getLocationFromHeader(Response post) {
        String location = post.getHeader("Location");
        Integer idFromLocation = null;
        if(location != null) {
            idFromLocation = Integer.valueOf(location.substring(location.lastIndexOf("/") + 1));
        }
        return idFromLocation;
    }

    public RestResponse<T> firstNotNull(boolean withValidation) {
        Response response = get(collectionUrl);

        if(withValidation) {
            response.then().statusCode(200);
        }

        T object = null;
        try {
            object = response.then().extract().body().jsonPath().getObject("content[0]", entityClass);
        } catch (Exception e) {
            if(withValidation) {
                throw new AssertionError("JSON unmarshalling error", e);
            }
        }

        return new RestResponse(response, object);
    }

    public RestResponse<T> firstNotNull() {
        return firstNotNull(true);
    }

    public RestResponse<T> get(int id, boolean withValidation) {
        Response response = get(collectionUrl + id);

        if(withValidation) {
            response.then().statusCode(200);
        }

        T object = null;
        try {
            object = response.jsonPath().getObject("content", entityClass);
        } catch (Exception e) {
            if(withValidation) {
                throw new AssertionError("JSON unmarshalling error", e);
            }
        }

        return new RestResponse(response, object);
    }

    public RestResponse<T> get(int id) {
        return get(id, true);
    }

    public RestResponse<T> createNew(T obj, boolean withValidation) {
        Response response = post(collectionUrl, obj);

        if(withValidation) {
            response.then().statusCode(201);
        }

        T object = null;
        try {
            object = response.then().extract().body().as(entityClass);
            System.out.println("#### JEST " + object);
        } catch (Exception e) {
            if(withValidation) {
                throw new AssertionError("JSON unmarshalling error", e);
            }
        }
        return new RestResponse(response, object);
    }

    public RestResponse<T> createNew(T obj) {
        return createNew(obj, true);
    }

    public RestResponse<T> update(int id, T obj, boolean withValidation) {
        Response response = put(collectionUrl + id, obj);

        if(withValidation) {
            response.then().statusCode(200);
        }

        return new RestResponse<>(response, get(id, withValidation).getValue());
    }

    public RestResponse<T> update(int id, T obj) {
        return update(id, obj, true);
    }

    public RestResponse<T> delete(int id, boolean withValidation) {
        Response response = delete(collectionUrl + id);

        if(withValidation) {
            response.then().statusCode(200);
        }

        return new RestResponse<>(response, null);
    }

    public RestResponse<T> delete(int id) {
        return delete(id, true);
    }

}
