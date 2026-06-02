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
package org.jboss.pnc.rest;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Filter to add SameSite=None attribute to session cookies (JSESSIONID). This is required for EAP 6.4 which does not
 * natively support SameSite cookie attribute.
 *
 * The HttpOnly and Secure flags are configured in web.xml via session-config.
 */
@WebFilter(filterName = "SameSiteCookieFilter", urlPatterns = { "/*" })
public class SameSiteCookieFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse httpResponse = (HttpServletResponse) response;

        SameSiteResponseWrapper wrappedResponse = new SameSiteResponseWrapper(httpResponse);
        chain.doFilter(request, wrappedResponse);
    }

    @Override
    public void destroy() {
    }

    private static class SameSiteResponseWrapper extends HttpServletResponseWrapper {

        public SameSiteResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void addHeader(String name, String value) {
            if ("Set-Cookie".equalsIgnoreCase(name)) {
                value = addSameSiteAttribute(value);
            }
            super.addHeader(name, value);
        }

        @Override
        public void setHeader(String name, String value) {
            if ("Set-Cookie".equalsIgnoreCase(name)) {
                value = addSameSiteAttribute(value);
            }
            super.setHeader(name, value);
        }

        private String addSameSiteAttribute(String cookieValue) {
            if (cookieValue == null) {
                return null;
            }

            // Check if cookie is JSESSIONID and doesn't already have SameSite attribute
            if (cookieValue.startsWith("JSESSIONID=") && !cookieValue.contains("SameSite=")) {
                return cookieValue + "; SameSite=None";
            }

            return cookieValue;
        }
    }
}
