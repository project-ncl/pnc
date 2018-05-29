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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import org.jboss.pnc.integration.client.util.RestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.jboss.pnc.integration.env.IntegrationTestEnv.getHttpPort;

public abstract class AbstractRestClient<T> {

    static class QueryParam {
        final String paramName;
        final String paramValue;

        public QueryParam(String paramName, String paramValue) {
            this.paramName = paramName;
            this.paramValue = paramValue;
        }
    }

    protected Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    protected String access_token = "no-auth";
    protected boolean authInitialized = false;
    public static final String CONTENT = "content";

    RestClient restClient;

    protected String collectionUrl;

    protected Class<T> entityClass;

    protected AbstractRestClient(String collectionUrl, Class<T> entityClass) {
        this(collectionUrl, entityClass, true);
    }

    protected AbstractRestClient(String collectionUrl, Class<T> entityClass, boolean withAuth) {
        this(collectionUrl, entityClass, ConnectionInfo.builder().port(getHttpPort()).basicAuth(new ConnectionInfo.BasicAuth("admin", "user.1234")).build());
    }

    protected AbstractRestClient(String collectionUrl, Class<T> entityClass, ConnectionInfo connectionInfo) {
        this.entityClass = entityClass;
        restClient = new RestClient(connectionInfo);

        if(collectionUrl.endsWith("/")) {
            this.collectionUrl = collectionUrl;
        } else {
            this.collectionUrl = collectionUrl + "/";
        }
    }

    /**
     * @deprecated use getRestClient().request()
     */
    @Deprecated
    protected RequestSpecification request() {
        return restClient.request();
    }

    /**
     * @deprecated use getRestClient().get()
     */
    @Deprecated
    protected Response get(String path, QueryParam... queryParam) {
        return restClient.get(path, queryParam);
    }

    /**
     * @deprecated use getRestClient().post()
     */
    @Deprecated
    protected Response post(String path) {
        return restClient.post(path);
    }

    /**
     * @deprecated use getRestClient().put()
     */
    @Deprecated
    protected Response put(String path, Object body) {
        return restClient.put(path, body);
    }

    protected RestClient getRestClient() {
        return restClient;
    }

    public RestResponse<T> firstNotNull(boolean withValidation) {
        Response response = restClient.get(collectionUrl);

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

    public RestResponse<T> post(String path, Object body, boolean withValidation) {
        Response response = getRestClient().post(path, body);

        if(withValidation) {
            response.then().statusCode(200);
        }

        T object = null;
        try {
            object = response.then().extract().body().jsonPath().getObject("", entityClass);
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
        Response response = restClient.get(collectionUrl + id);

        if(withValidation) {
            response.then().statusCode(200);
        }

        T object = null;
        try {
            object = response.jsonPath().getObject(CONTENT, entityClass);
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
        Response response = restClient.post(collectionUrl, obj);

        if(withValidation) {
            response.then().statusCode(201);
        }

        T object = null;
        try {
            object = response.thenReturn().jsonPath().getObject(CONTENT, entityClass);
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
        Response response = restClient.put(collectionUrl + id, obj);

        if(withValidation) {
            response.then().statusCode(200);
        }

        return new RestResponse<>(response, get(id, withValidation).getValue());
    }

    public RestResponse<T> update(int id, T obj) {
        return update(id, obj, true);
    }

    public RestResponse<T> delete(int id, boolean withValidation) {
        Response response = restClient.delete(collectionUrl + id);

        if(withValidation) {
            response.then().statusCode(200);
        }

        return new RestResponse<>(response, null);
    }

    public RestResponse<T> delete(int id) {
        return delete(id, true);
    }

    public RestResponse<List<T>> all(boolean withValidation, int pageIndex, int pageSize, String rsql, String sort) {
        return all(entityClass, collectionUrl, withValidation, pageIndex, pageSize, rsql, sort);
    }

    public RestResponse<List<T>> all() {
        return all(true, 0, 50, null, null);
    }

    protected final <U> RestResponse<List<U>> all (Class<U> type, String url, boolean withValidation, int pageIndex, int pageSize, String rsql, String sort) {
        QueryParam rsqlQueryParam = null;
        QueryParam sortQueryParam = null;
        if(rsql != null) {
            rsqlQueryParam = new QueryParam("q", rsql);
        }
        if(sort != null) {
            sortQueryParam = new QueryParam("sort", sort);
        }
        QueryParam pageIndexQueryParam = new QueryParam("pageIndex", Integer.toString(pageIndex));
        QueryParam pageSizeQueryParam = new QueryParam("pageSize", "" + Integer.toString(pageSize));
        Response response = restClient.get(url, rsqlQueryParam, sortQueryParam, pageIndexQueryParam, pageSizeQueryParam);

        logger.info("response {} ", response.prettyPrint());

        List<U> object = new ArrayList<>();
        String responseBody = response.getBody().asString();

        // Response body may be null, if the query did not return any result! This would need to deliver a response with 204
        // status, with no errors
        if (responseBody != null && !responseBody.isEmpty()) {
            try {
                List<? extends Map> beforeMappingList = response.jsonPath().getList(CONTENT);
                ObjectMapper objectMapper = new ObjectMapper();
                for (Map obj : beforeMappingList) {
                    // because of the bug in RestAssured - we need to use another mapping library...
                    object.add(objectMapper.convertValue(obj, type));
                }
            } catch (Exception e) {
                logger.error("JSON unmarshalling error", e);
                if (withValidation) {
                    throw new AssertionError("JSON unmarshalling error", e);
                }
            }
        }
        if (withValidation) {
            if (!object.isEmpty()) {
                response.then().statusCode(200);
            } else {
                response.then().statusCode(204);
            }
        }

        return new RestResponse<>(response, object);
    }
}
