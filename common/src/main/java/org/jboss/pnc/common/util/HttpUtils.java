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

import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;

/**
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 */
public class HttpUtils {

    private HttpUtils() {
    }

    /**
     * Process HTTP GET request and get the data as type specified as parameter.
     * Client accepts application/json MIME type.
     *
     * @param clazz Class to which the data are unmarshalled
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
}
