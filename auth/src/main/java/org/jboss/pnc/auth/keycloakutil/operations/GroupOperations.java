/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.auth.keycloakutil.operations;

import static org.jboss.pnc.auth.keycloakutil.util.HttpUtil.composeResourceUrl;
import static org.jboss.pnc.auth.keycloakutil.util.HttpUtil.doDeleteJSON;
import static org.jboss.pnc.auth.keycloakutil.util.HttpUtil.doPostJSON;
import static org.jboss.pnc.auth.keycloakutil.util.HttpUtil.getIdForType;

import java.util.List;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class GroupOperations {

    public static String getIdFromName(String rootUrl, String realm, String auth, String groupname) {
        return getIdForType(rootUrl, realm, auth, "groups", "name", groupname);
    }

    public static String getIdFromPath(String rootUrl, String realm, String auth, String path) {
        return getIdForType(rootUrl, realm, auth, "groups", "path", path);
    }

    public static void addRealmRoles(String rootUrl, String realm, String auth, String groupid, List<?> roles) {
        String resourceUrl = composeResourceUrl(rootUrl, realm, "groups/" + groupid + "/role-mappings/realm");
        doPostJSON(resourceUrl, auth, roles);
    }

    public static void addClientRoles(
            String rootUrl,
            String realm,
            String auth,
            String groupid,
            String idOfClient,
            List<?> roles) {
        String resourceUrl = composeResourceUrl(
                rootUrl,
                realm,
                "groups/" + groupid + "/role-mappings/clients/" + idOfClient);
        doPostJSON(resourceUrl, auth, roles);
    }

    public static void removeRealmRoles(String rootUrl, String realm, String auth, String groupid, List<?> roles) {
        String resourceUrl = composeResourceUrl(rootUrl, realm, "groups/" + groupid + "/role-mappings/realm");
        doDeleteJSON(resourceUrl, auth, roles);
    }

    public static void removeClientRoles(
            String rootUrl,
            String realm,
            String auth,
            String groupid,
            String idOfClient,
            List<?> roles) {
        String resourceUrl = composeResourceUrl(
                rootUrl,
                realm,
                "groups/" + groupid + "/role-mappings/clients/" + idOfClient);
        doDeleteJSON(resourceUrl, auth, roles);
    }
}
