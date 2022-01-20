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
package org.jboss.pnc.client;

import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.jboss.resteasy.client.jaxrs.ClientHttpEngine;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient43Engine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class ApacheHttpClient43EngineWithRetry extends ApacheHttpClient43Engine implements ClientHttpEngine {

    private static final Logger logger = LoggerFactory.getLogger(ApacheHttpClient43EngineWithRetry.class);

    @Override
    protected HttpClient createDefaultHttpClient() {
        logger.info("Bootstrapping http engine with request retry handler...");
        final HttpClientBuilder builder = HttpClientBuilder.create();
        RequestConfig.Builder requestBuilder = RequestConfig.custom();
        if (defaultProxy != null) {
            requestBuilder.setProxy(defaultProxy);
        }
        builder.disableContentCompression();
        builder.setDefaultRequestConfig(requestBuilder.build());

        HttpRequestRetryHandler retryHandler = new StandardHttpRequestRetryHandler();
        builder.setRetryHandler(retryHandler);
        return builder.build();
    }
}
