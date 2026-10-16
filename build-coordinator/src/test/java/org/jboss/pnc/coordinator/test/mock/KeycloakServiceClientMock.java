/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.coordinator.test.mock;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.pnc.auth.KeycloakServiceClient;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@ApplicationScoped
public class KeycloakServiceClientMock implements KeycloakServiceClient {

    @Override
    public String getAuthToken() {
        return "mocked-token";
    }
}
