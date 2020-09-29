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
package org.jboss.pnc.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

/**
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 */
public class HttpUtils {

    private static final Logger LOG = LoggerFactory.getLogger(HttpUtils.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private HttpUtils() {
    }

    /**
     * Process HTTP GET request and get the data as type specified as parameter. Client accepts application/json MIME
     * type.
     *
     * @param clazz Class to which the data are unmarshalled
     * @param <T> module config
     * @param url Request URL
     * @throws Exception Thrown if some error occurs in communication with server
     * @return Unmarshalled entity data
     */
    public static <T> T processGetRequest(Class<T> clazz, String url) throws Exception {
        ClientRequest request = new ClientRequest(url);
        request.accept(MediaType.APPLICATION_JSON);

        ClientResponse<T> response = request.get(clazz);
        return response.getEntity();
    }

    public static String processGetRequest(String url) throws Exception {
        ClientRequest request = new ClientRequest(url);
        request.accept(MediaType.APPLICATION_JSON);
        ClientResponse<String> response = request.get(String.class);
        return response.getEntity();
    }

    /**
     * Process HTTP requests and tests if server responds with expected HTTP code. Request is implicitly set to accept
     * MIME type application/json.
     *
     * @param ecode Expected HTTP error code
     * @param url Request URL
     * @throws Exception Thrown if some error occurs in communication with server
     */
    public static void testResponseHttpCode(int ecode, String url) throws Exception {
        ClientRequest request = new ClientRequest(url);
        request.accept(MediaType.APPLICATION_JSON);

        ClientResponse<String> response = request.get(String.class);
        if (response.getStatus() != ecode)
            throw new Exception("Server returned unexpected HTTP code! Returned code:" + response.getStatus());
    }

    /**
     *
     * NOTE: Be sure to close the HTTP connection after every request!
     * 
     * @param retries - int number of retries to execute request in case of failure
     * @return Closeable "permissive" HttpClient instance, ignoring invalid SSL certificates.
     */
    public static CloseableHttpClient getPermissiveHttpClient(int retries) {
        SSLContextBuilder builder = new SSLContextBuilder();
        try {
            builder.loadTrustMaterial(null, (chain, authType) -> true);
        } catch (NoSuchAlgorithmException | KeyStoreException e1) {
            LOG.error("Error creating SSL Context Builder with trusted certificates.", e1);
        }

        SSLConnectionSocketFactory sslSF = null;
        try {
            sslSF = new SSLConnectionSocketFactory(builder.build(), NoopHostnameVerifier.INSTANCE);
        } catch (KeyManagementException | NoSuchAlgorithmException e1) {
            LOG.error("Error creating SSL Connection Factory.", e1);
        }

        return HttpClients.custom()
                .setRetryHandler(new DefaultHttpRequestRetryHandler(retries, false))
                .setSSLSocketFactory(sslSF)
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build();
    }

    /**
     * Performs HTTP post request to a uri with a json payload
     *
     * @param uri URI of a remote endpoint
     * @param object Request content, which will be serialized to JSON format
     */
    public static void performHttpPostRequest(String uri, Object object) throws JsonProcessingException {
        StringEntity entity = new StringEntity(objectMapper.writeValueAsString(object), "UTF-8");
        entity.setContentType(MediaType.APPLICATION_JSON);
        performHttpPostRequest(uri, entity, Optional.empty());
    }

    /**
     * Performs HTTP post request to a uri with a json payload
     *
     * @param uri URI of a remote endpoint
     * @param jsonPayload Request content, already formatted as json string
     * @param authToken The authorization token
     */
    public static void performHttpPostRequest(String uri, String jsonPayload, String authToken)
            throws JsonProcessingException {
        StringEntity entity = new StringEntity(jsonPayload, "UTF-8");
        entity.setContentType(MediaType.APPLICATION_JSON);
        performHttpPostRequest(uri, entity, Optional.ofNullable(authToken));
    }

    /**
     * Performs HTTP POST request to a uri with a payload
     *
     * @param uri URI of a remote endpoint
     * @param payload Request content
     * @param authToken The authorization token
     */
    public static void performHttpPostRequest(String uri, HttpEntity payload, Optional<String> authToken) {
        LOG.debug("Sending HTTP POST request to {} with payload {}", uri, payload);

        HttpPost request = new HttpPost(uri);
        request.setEntity(payload);
        if (authToken.isPresent()) {
            request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + authToken.get());
        }

        try (CloseableHttpClient httpClient = HttpUtils.getPermissiveHttpClient()) {
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                if (isSuccess(response.getStatusLine().getStatusCode())) {
                    LOG.debug(
                            "HTTP POST request to {} with payload {} sent successfully. Response code: {}",
                            uri,
                            payload,
                            response.getStatusLine().getStatusCode());
                } else {
                    LOG.error(
                            "Sending HTTP POST request to {} with payload {} failed! "
                                    + "Response code: {}, Message: {}",
                            uri,
                            payload,
                            response.getStatusLine().getStatusCode(),
                            response.getEntity().getContent());
                }
            }
        } catch (IOException e) {
            LOG.error("Error occurred executing the HTTP post request!", e);
        }
    }

    /**
     * @return Closeable "permissive" HttpClient instance, ignoring invalid SSL certificates, using 3 attempts to retry
     *         failed request
     * @see #getPermissiveHttpClient(int)
     */
    public static CloseableHttpClient getPermissiveHttpClient() {
        return getPermissiveHttpClient(3);
    }

    public static boolean isSuccess(int statusCode) {
        return statusCode / 100 == 2;
    }

}
