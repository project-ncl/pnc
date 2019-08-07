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

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebListener;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebListener
public class PNCStartupLogger implements ServletContextListener, ServletContainerInitializer {

    public static final Logger log = Logger.getLogger(PNCStartupLogger.class.getName());

    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {

    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        log.info("Starting up PNC " + getManifestInformation());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("Shutting down PNC");
    }

    private String getManifestInformation() {
        String result = "";
        try {
            final Enumeration<URL> resources = PNCStartupLogger.class.getClassLoader().getResources("META-INF/MANIFEST.MF");

            while (resources.hasMoreElements()) {
                final URL jarUrl = resources.nextElement();

                log.fine("Processing jar resource " + jarUrl);
                if (jarUrl.getFile().contains("pnc-web")) {
                    final Manifest manifest = new Manifest(jarUrl.openStream());
                    result = manifest.getMainAttributes().getValue("Implementation-Version");
                    result += " ( SHA: " + manifest.getMainAttributes().getValue("Scm-Revision") + " ) ";
                    break;
                }
            }
        } catch (final IOException e) {
            log.log(Level.SEVERE, "Error retrieving information from manifest", e);
        }

        return result;
    }
}
