/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.auth.keycloakutil.operations;

import static org.jboss.pnc.auth.keycloakutil.util.HttpUtil.getIdForType;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class ClientOperations {

    public static String getIdFromClientId(String rootUrl, String realm, String auth, String clientId) {
        return getIdForType(rootUrl, realm, auth, "clients", "clientId", clientId);
    }
}
