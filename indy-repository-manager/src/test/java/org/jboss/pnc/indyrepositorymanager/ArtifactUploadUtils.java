/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.indyrepositorymanager;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 5/10/16 Time: 2:15 PM
 */
public class ArtifactUploadUtils {
    public static boolean put(CloseableHttpClient client, String url, String content) throws IOException {
        HttpPut put = new HttpPut(url);
        put.setEntity(new StringEntity(content));
        return client.execute(put, response -> {
            try {
                return response.getStatusLine().getStatusCode() == 201;
            } finally {
                if (response instanceof CloseableHttpResponse) {
                    IOUtils.closeQuietly((CloseableHttpResponse) response);
                }
            }
        });
    }
}
