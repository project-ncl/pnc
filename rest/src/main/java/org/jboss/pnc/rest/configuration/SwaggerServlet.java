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
package org.jboss.pnc.rest.configuration;

import io.swagger.config.ScannerFactory;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.config.DefaultJaxrsScanner;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

@WebServlet(name = "SwaggerJaxrsConfig", loadOnStartup = 1)
public class SwaggerServlet extends HttpServlet {

    @Override
    public void init() {
        try {
            BeanConfig swaggerConfig = new BeanConfig();
            swaggerConfig.setVersion("1.0.0");
            swaggerConfig.setBasePath(getBaseUrl());
            swaggerConfig.setScan(true);
            swaggerConfig.setPrettyPrint(true);
            ScannerFactory.setScanner(new DefaultJaxrsScanner());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    String getBaseUrl() throws IOException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("/swagger.properties");
        Properties properties = new Properties();
        try (InputStream inStream = resource.openStream()) {
            properties.load(inStream);
        }
        return properties.getProperty("baseUrl");
    }

}
