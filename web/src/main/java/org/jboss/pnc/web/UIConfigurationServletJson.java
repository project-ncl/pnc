/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;

/**
 * Dynamically serves a configuration file for the UI.
 *
 * @author Alex Creasy
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 */
@WebServlet("/config.json")
@Dependent
public class UIConfigurationServletJson extends HttpServlet {

    public static final int CACHE_EXPIRES_IN = 0; // Cache time in seconds.

    @Inject
    private Configuration configuration;

    private String uiConfig;

    @Override
    public void init() throws ServletException {
        try {
            UiConfigRest configRest = UiConfigRestBuilder.build(configuration);
            this.uiConfig = JsonOutputConverterMapper.apply(configRest);
        } catch (ConfigurationParseException e) {
            throw new ServletException(
                    "Lazy-loading of UI configuration failed because the servlet was not able to fetch the configuration.",
                    e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");

        // Override our default javascript cache time of 10 years as we're not using the standard cache
        // defeating mechanism here.
        resp.setHeader("Cache-Control", "max-age=" + CACHE_EXPIRES_IN);

        PrintWriter writer = resp.getWriter();
        writer.println(uiConfig);
        writer.flush();
    }
}
