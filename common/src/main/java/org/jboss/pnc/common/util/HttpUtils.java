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
package org.jboss.pnc.common.util;

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;

import javax.ws.rs.core.MediaType;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 */
public class HttpUtils {

    private static final Logger LOG = LoggerFactory.getLogger(HttpUtils.class);

    private HttpUtils() {
    }

    /**
     * Process HTTP GET request and get the data as type specified as parameter.
     * Client accepts application/json MIME type.
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
     * Process HTTP requests and tests if server responds with expected HTTP code.
     * Request is implicitly set to accept MIME type application/json.
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
     * @param retries - int number of retries to execute request in case of failure
     * @return Closeable "permissive" HttpClient instance, ignoring invalid SSL certificates.
     */
    public static CloseableHttpClient getPermissiveHttpClient(int retries) {
        SSLContextBuilder builder = new SSLContextBuilder();
        try {
            builder.loadTrustMaterial(null, new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            });
        } catch (NoSuchAlgorithmException | KeyStoreException e1) {
            LOG.error("Error creating SSL Context Builder with trusted certificates.", e1);
        }

        SSLConnectionSocketFactory sslSF = null;
        try {
            sslSF = new SSLConnectionSocketFactory(builder.build(), SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        } catch (KeyManagementException | NoSuchAlgorithmException e1) {
            LOG.error("Error creating SSL Connection Factory.", e1);
        }

        CloseableHttpClient httpclient = HttpClients.custom()
                .setRetryHandler(new DefaultHttpRequestRetryHandler(retries, false))
                .setSSLSocketFactory(sslSF)
                .setHostnameVerifier(new AllowAllHostnameVerifier()).build();

        return httpclient;
    }

    /**
     * @return Closeable "permissive" HttpClient instance, ignoring invalid SSL certificates, using 3 attempts to retry failed request 
     * @see getPermissiveHttpClient(int retries)
     */
    public static CloseableHttpClient getPermissiveHttpClient() {
        return getPermissiveHttpClient(3);
    }

    public static boolean isSuccess(int statusCode) {
        return statusCode / 100 == 2;
    }

}
