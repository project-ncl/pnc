/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.auth.keycloakutil.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.jboss.pnc.auth.AuthenticationException;
import org.jboss.pnc.auth.keycloakutil.httpcomponents.HttpDelete;
import org.jboss.pnc.auth.keycloakutil.operations.LocalSearch;
import org.jboss.pnc.auth.keycloakutil.operations.RoleOperations;
import org.keycloak.util.JsonSerialization;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.keycloak.common.util.ObjectUtil.capitalize;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class HttpUtil {

    public static final String APPLICATION_XML = "application/xml";
    public static final String APPLICATION_JSON = "application/json";
    public static final String APPLICATION_FORM_URL_ENCODED = "application/x-www-form-urlencoded";
    public static final String UTF_8 = "utf-8";

    private static HttpClient httpClient;
    private static SSLConnectionSocketFactory sslsf;
    private static boolean sslRequired;

    public static InputStream doGet(String url, String acceptType, String authorization) {
        try {
            HttpGet request = new HttpGet(url);
            request.setHeader(HttpHeaders.ACCEPT, acceptType);
            return doRequest(authorization, request);
        } catch (IOException e) {
            throw new RuntimeException("Failed to send request - " + e.getMessage(), e);
        }
    }

    public static InputStream doPost(String url, String contentType, String acceptType, String content, String authorization) {
        try {
            return doPostOrPut(contentType, acceptType, content, authorization, new HttpPost(url));
        } catch (IOException e) {
            throw new RuntimeException("Failed to send request - " + e.getMessage(), e);
        }
    }

    public static InputStream doPut(String url, String contentType, String acceptType, String content, String authorization) {
        try {
            return doPostOrPut(contentType, acceptType, content, authorization, new HttpPut(url));
        } catch (IOException e) {
            throw new RuntimeException("Failed to send request - " + e.getMessage(), e);
        }
    }

    public static void doDelete(String url, String authorization) {
        try {
            HttpDelete request = new HttpDelete(url);
            doRequest(authorization, request);
        } catch (IOException e) {
            throw new RuntimeException("Failed to send request - " + e.getMessage(), e);
        }
    }


    public static HeadersBodyStatus doGet(String url, HeadersBody request) throws IOException {
        return doRequest("get", url, request);
    }

    public static HeadersBodyStatus doPost(String url, HeadersBody request) throws IOException {
        return doRequest("post", url, request);
    }

    public static HeadersBodyStatus doPut(String url, HeadersBody request) throws IOException {
        return doRequest("put", url, request);
    }

    public static HeadersBodyStatus doDelete(String url, HeadersBody request) throws IOException {
        return doRequest("delete", url, request);
    }

    public static HeadersBodyStatus doRequest(String type, String url, HeadersBody request) throws IOException {
        HttpRequestBase req;
        switch (type) {
            case "get":
                req = new HttpGet(url);
                break;
            case "post":
                req = new HttpPost(url);
                break;
            case "put":
                req = new HttpPut(url);
                break;
            case "delete":
                req = new HttpDelete(url);
                break;
            case "options":
                req = new HttpOptions(url);
                break;
            case "head":
                req = new HttpHead(url);
                break;
            default:
                throw new RuntimeException("Method not supported: " + type);
        }
        addHeaders(req, request.getHeaders());

        if (request.getBody() != null) {
            if (req instanceof HttpEntityEnclosingRequestBase == false) {
                throw new RuntimeException("Request type does not support body: " + type);
            }
            ((HttpEntityEnclosingRequestBase) req).setEntity(new InputStreamEntity(request.getBody()));
        }

        HttpResponse res = getHttpClient().execute(req);
        InputStream responseStream = null;
        if (res.getEntity() != null) {
            responseStream = res.getEntity().getContent();
        } else {
            responseStream = new InputStream() {
                @Override
                public int read () throws IOException {
                    return -1;
                }
            };
        }

        Headers headers = new Headers();
        HeaderIterator it = res.headerIterator();
        while (it.hasNext()) {
            org.apache.http.Header header = it.nextHeader();
            headers.add(header.getName(), header.getValue());
        }

        return new HeadersBodyStatus(res.getStatusLine().toString(), headers, responseStream);
    }

    private static void addHeaders(HttpRequestBase request, Headers headers) {
        for (Header header: headers) {
            request.setHeader(header.getName(), header.getValue());
        }
    }

    private static InputStream doPostOrPut(String contentType, String acceptType, String content, String authorization, HttpEntityEnclosingRequestBase request) throws IOException {
        request.setHeader(HttpHeaders.CONTENT_TYPE, contentType);
        request.setHeader(HttpHeaders.ACCEPT, acceptType);
        if (content != null) {
            request.setEntity(new StringEntity(content));
        }

        return doRequest(authorization, request);
    }

    private static InputStream doRequest(String authorization, HttpRequestBase request) throws IOException {
        addAuth(request, authorization);

        HttpResponse response = getHttpClient().execute(request);
        InputStream responseStream = null;
        if (response.getEntity() != null) {
            responseStream = response.getEntity().getContent();
        }

        int code = response.getStatusLine().getStatusCode();
        if (code >= 200 && code < 300) {
            return responseStream;
        } else {
            Map<String, String> error = null;
            try {
                org.apache.http.Header header = response.getEntity().getContentType();
                if (header != null && APPLICATION_JSON.equals(header.getValue())) {
                    error = JsonSerialization.readValue(responseStream, Map.class);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to read error response - " + e.getMessage(), e);
            } finally {
                responseStream.close();
            }

            String message = null;
            if (error != null) {
                message = error.get("error_description") + " [" + error.get("error") + "]";
            }
            throw new RuntimeException(message != null ? message : response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
        }
    }

    private static void addAuth(HttpRequestBase request, String authorization) {
        if (authorization != null) {
            request.setHeader(HttpHeaders.AUTHORIZATION, authorization);
        }
    }

    public static HttpClient getHttpClient() {
        if (httpClient == null) {
            HttpClientBuilder clientBuilder = HttpClientBuilder.create().useSystemProperties();
            if (sslRequired) {
                if (sslsf != null) {
                    clientBuilder.setSSLSocketFactory(sslsf);
                }
            } else {
                SSLContext sslContext;
                try {
                    sslContext = SSLContext.getInstance("SSL");

                    // set up a TrustManager that trusts everything
                    sslContext.init(null, new TrustManager[] { new X509TrustManager() {

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        }
                    } }, new SecureRandom());
                } catch (NoSuchAlgorithmException ex) {
                    throw new AuthenticationException("Cannot get SSLContext instance for \"SSL\" protocol.", ex);
                } catch (KeyManagementException ex) {
                    throw new AuthenticationException("SSLContext initialization failed.", ex);
                }

                clientBuilder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).setSSLContext(sslContext);
            }
            httpClient = clientBuilder.build();
        }
        return httpClient;
    }

    public static String urlencode(String value) {
        try {
            return URLEncoder.encode(value, UTF_8);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to urlencode", e);
        }
    }

    /**
     * Sets flag telling if SSL hostname validation should be done which also clears the cached httpClient. This method
     * is not thread-safe and could cause race conditions if the class is used with different settings at the same
     * time, but that is not expected.
     *
     * @param sslRequired the desired value
     */
    public static void setSslRequired(boolean sslRequired) {
        if (HttpUtil.sslRequired != sslRequired) {
            HttpUtil.sslRequired = sslRequired;
            HttpUtil.httpClient = null;
        }
    }

    public static void setTruststore(File file, String password) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, KeyManagementException {
        if (!file.isFile()) {
            throw new RuntimeException("Truststore file not found: " + file.getAbsolutePath());
        }
        SSLContext theContext = SSLContexts.custom()
                .useProtocol("TLS")
                .loadTrustMaterial(file, password == null ? null : password.toCharArray())
                .build();
        sslsf = new SSLConnectionSocketFactory(theContext);
    }

    public static String extractIdFromLocation(String location) {
        int last = location.lastIndexOf("/");
        if (last != -1) {
            return location.substring(last + 1);
        }
        return null;
    }

    public static String addQueryParamsToUri(String uri, String ... queryParams) {
        if (queryParams == null) {
            return uri;
        }

        if (queryParams.length % 2 != 0) {
            throw new RuntimeException("Value missing for query parameter: " + queryParams[queryParams.length-1]);
        }

        Map<String, String> params = new LinkedHashMap<>();
        for (int i = 0; i < queryParams.length; i += 2) {
            params.put(queryParams[i], queryParams[i+1]);
        }
        return addQueryParamsToUri(uri, params);
    }

    public static String addQueryParamsToUri(String uri, Map<String, String> queryParams) {

        if (queryParams.size() == 0) {
            return uri;
        }

        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> params: queryParams.entrySet()) {
            try {
                if (query.length() > 0) {
                    query.append("&");
                }
                query.append(params.getKey()).append("=").append(URLEncoder.encode(params.getValue(), "utf-8"));
            } catch (Exception e) {
                throw new RuntimeException("Failed to encode query params: " + params.getKey() + "=" + params.getValue());
            }
        }

        return uri + (uri.indexOf("?") == -1 ? "?" : "&") + query;
    }

    public static String composeResourceUrl(String adminRoot, String realm, String uri) {
        if (!uri.startsWith("http:") && !uri.startsWith("https:")) {
            if ("realms".equals(uri) || uri.startsWith("realms/")) {
                uri = normalize(adminRoot) + uri;
            } else if ("serverinfo".equals(uri)) {
                    uri = normalize(adminRoot) + uri;
            } else {
                uri = normalize(adminRoot) + "realms/" + realm + "/" + uri;
            }
        }
        return uri;
    }

    public static String normalize(String value) {
        return value.endsWith("/") ? value : value + "/";
    }

    public static void checkSuccess(String url, HeadersBodyStatus response) {
        try {
            response.checkSuccess();
        } catch (HttpResponseException e) {
            if (e.getStatusCode() == 404) {
                throw new RuntimeException("Resource not found for url: " + url, e);
            }
            throw e;
        }
    }

    public static <T> T doGetJSON(Class<T> type, String resourceUrl, String auth) {

        Headers headers = new Headers();
        if (auth != null) {
            headers.add("Authorization", auth);
        }
        headers.add("Accept", "application/json");

        HeadersBodyStatus response;
        try {
            response = HttpUtil.doRequest("get", resourceUrl, new HeadersBody(headers));
        } catch (IOException e) {
            throw new RuntimeException("HTTP request failed: GET " + resourceUrl, e);
        }

        checkSuccess(resourceUrl, response);

        T result;
        try {
            result = JsonSerialization.readValue(response.getBody(), type);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON response", e);
        }

        return result;
    }

    public static void doPostJSON(String resourceUrl, String auth, Object content) {
        Headers headers = new Headers();
        if (auth != null) {
            headers.add("Authorization", auth);
        }
        headers.add("Content-Type", "application/json");

        HeadersBodyStatus response;

        byte[] body;
        try {
            body = JsonSerialization.writeValueAsBytes(content);
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize JSON", e);
        }

        try {
            response = HttpUtil.doRequest("post", resourceUrl, new HeadersBody(headers, new ByteArrayInputStream(body)));
        } catch (IOException e) {
            throw new RuntimeException("HTTP request failed: POST " + resourceUrl + "\n" + new String(body), e);
        }

        checkSuccess(resourceUrl, response);
    }

    public static void doDeleteJSON(String resourceUrl, String auth, Object content) {
        Headers headers = new Headers();
        if (auth != null) {
            headers.add("Authorization", auth);
        }
        headers.add("Content-Type", "application/json");

        HeadersBodyStatus response;

        byte[] body;
        try {
            body = JsonSerialization.writeValueAsBytes(content);
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize JSON", e);
        }

        try {
            response = HttpUtil.doRequest("delete", resourceUrl, new HeadersBody(headers, new ByteArrayInputStream(body)));
        } catch (IOException e) {
            throw new RuntimeException("HTTP request failed: DELETE " + resourceUrl + "\n" + new String(body), e);
        }

        checkSuccess(resourceUrl, response);
    }

    public static String getIdForType(String rootUrl, String realm, String auth, String resourceEndpoint, String attrName, String attrValue) {

        return getAttrForType(rootUrl, realm, auth, resourceEndpoint, attrName, attrValue, "id");
    }

    public static String getAttrForType(String rootUrl, String realm, String auth, String resourceEndpoint, String attrName, String attrValue, String returnAttrName) {

        String resourceUrl = composeResourceUrl(rootUrl, realm, resourceEndpoint);
        resourceUrl = HttpUtil.addQueryParamsToUri(resourceUrl, attrName, attrValue, "first", "0", "max", "2");

        List<ObjectNode> users = doGetJSON(RoleOperations.LIST_OF_NODES.class, resourceUrl, auth);

        ObjectNode user;
        try {
            user = new LocalSearch(users).exactMatchOne(attrValue, attrName);
        } catch (Exception e) {
            throw new RuntimeException("Multiple " + resourceEndpoint + " found for " + attrName + ": " + attrValue, e);
        }

        String typeName = singularize(resourceEndpoint);
        if (user == null) {
            throw new RuntimeException(capitalize(typeName) + " not found for " + attrName + ": " + attrValue);
        }

        JsonNode attr = user.get(returnAttrName);
        if (attr == null) {
            throw new RuntimeException("Returned " + typeName + " info has no '" + returnAttrName + "' attribute");
        }
        return attr.asText();
    }


    public static String singularize(String value) {
        return value.substring(0, value.length()-1);
    }
}
