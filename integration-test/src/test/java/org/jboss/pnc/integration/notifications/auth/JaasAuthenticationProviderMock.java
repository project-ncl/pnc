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
package org.jboss.pnc.integration.notifications.auth;

import org.jboss.pnc.auth.AuthProvider;
import org.jboss.pnc.auth.AuthenticationProvider;
import org.jboss.pnc.auth.LoggedInUser;
import org.jboss.pnc.auth.NoAuthLoggedInUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Dependent;
import javax.servlet.http.HttpServletRequest;

/**
 * Authentication provider which acts as a {@link org.jboss.pnc.auth.NoAuthAuthenticationProvider}, however, it has got
 * the ID of the {@link org.jboss.pnc.auth.JaasAuthenticationProvider}.
 */
@Dependent
@AuthProvider
public class JaasAuthenticationProviderMock implements AuthenticationProvider {

    public static final String ID = "JAAS";

    public final static Logger log = LoggerFactory.getLogger(org.jboss.pnc.auth.NoAuthAuthenticationProvider.class);

    private HttpServletRequest httpServletRequest = null;

    public JaasAuthenticationProviderMock() {
    }

    @Override
    public LoggedInUser getLoggedInUser(HttpServletRequest httpServletRequest) {
        return new NoAuthLoggedInUser();
    }

    @Override
    public String getId() {
        return ID;
    }

}
