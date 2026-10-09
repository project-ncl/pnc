/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.integration.mock.client;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.pnc.auth.ServiceAccountClient;

@ApplicationScoped
public class ServiceAccountClientMock implements ServiceAccountClient {

    @Override
    public String getAuthHeaderValue() {
        return "Basic 1234";
    }
}
