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
package org.jboss.pnc.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class JAASLoggedInUser implements LoggedInUser {

    public final static Logger log = LoggerFactory.getLogger(JAASLoggedInUser.class);

    private HttpServletRequest httpServletRequest;

    public JAASLoggedInUser(HttpServletRequest httpServletRequest) {
        if (httpServletRequest == null) {
            throw new NullPointerException();
        }
        this.httpServletRequest = httpServletRequest;
        log.debug("Instantiated new object for username: {}.", getUserName());
    }

    @Override
    public String getEmail() {
        return getUserName() + "@" + "not.available";
    }

    @Override
    public String getUserName() {
        if (httpServletRequest.getUserPrincipal() == null) {
            return null;
        }
        return httpServletRequest.getUserPrincipal().getName();
    }

    @Override
    public String getFirstName() {
        return "First Name N/A (" + getUserName() + ")";
    }

    @Override
    public String getLastName() {
        return "Last Name N/A (" + getUserName() + ")";
    }

    @Override
    public Set<String> getRole() {
        throw new UnsupportedOperationException("Role is not available. Use isUserInRole instead.");
    }

    @Override
    public boolean isUserInRole(String role) {
        return httpServletRequest.isUserInRole(role);
    }

    @Override
    public String getTokenString() {
        return "--NO-TOKEN-AVAILABLE--";
    }

}
