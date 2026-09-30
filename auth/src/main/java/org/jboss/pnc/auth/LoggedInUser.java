/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.auth;

import java.util.Set;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public interface LoggedInUser {
    String getEmail();

    String getUserName();

    String getFirstName();

    String getLastName();

    Set<String> getRole();

    boolean isUserInRole(String role);

    String getTokenString();
}
