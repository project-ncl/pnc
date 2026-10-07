/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.auth;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.LDAPClientConfig;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;

/**
 * Implementaiton of LDAP Service Client
 */
@ApplicationScoped
public class DefaultLDAPServiceClient implements LDAPServiceClient {

    private LDAPClientConfig ldapClientConfig;

    @Deprecated // CDI workaround
    public DefaultLDAPServiceClient() {
    }

    @Inject
    public DefaultLDAPServiceClient(SystemConfig systemConfig) throws ConfigurationParseException {
        ldapClientConfig = systemConfig.getLdapClientConfig();
    }

    @Override
    public String getHeader() {
        return "Basic " + ldapClientConfig.getBase64UsernameAndPassword();
    }
}
