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
package org.jboss.pnc.web;

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.UIModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.rest.utils.JsonOutputConverterMapper;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Dynamically serves a configuration file for the UI.
 *
 * @author Alex Creasy
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 */
@WebServlet("/scripts/config.js")
public class UIConfigurationServlet extends HttpServlet {

    public static final int CACHE_EXPIRES_IN = 0; // Cache time in seconds.
    public static final String PNC_GLOBAL_MODULE = "pnc";
    public static final String CONFIG_PROPERTY = "config";

    @Inject
    private Configuration configuration;
    
    private String uiConfig;

    private void lazyLoadUiConfig() throws ServletException {
        try {
            UIModuleConfig uiConfig = configuration.getModuleConfig(new PncConfigProvider<>(UIModuleConfig.class));
            String json = JsonOutputConverterMapper.apply(uiConfig);
            this.uiConfig = generateJS(json);
        } catch (ConfigurationParseException e) {
            throw new ServletException("Lazy-loading of UI configuration failed because the servlet was not able to fetch the configuration.", e);
        }
    }

    private String generateJS(String configJson) {
        return String.format("var %1$s = %1$s || {}; %1$s.%2$s = %3$s; window.%1$s = %1$s;", PNC_GLOBAL_MODULE, CONFIG_PROPERTY, configJson);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (uiConfig == null) {
            synchronized (this) {
                if (uiConfig == null) {
                    lazyLoadUiConfig();
                }
            }
        }

        resp.setContentType("text/javascript");

        // Override our default javascript cache time of 10 years as we're not using the standard cache
        // defeating mechanism here.
        resp.setHeader("Cache-Control", "max-age=" + CACHE_EXPIRES_IN);

        PrintWriter writer = resp.getWriter();
        writer.println(uiConfig);
        writer.flush();
    }
}
