/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.auth.keycloakutil.operations;

import java.util.List;

import static org.jboss.pnc.auth.keycloakutil.util.HttpUtil.composeResourceUrl;
import static org.jboss.pnc.auth.keycloakutil.util.HttpUtil.doDeleteJSON;
import static org.jboss.pnc.auth.keycloakutil.util.HttpUtil.doPostJSON;
import static org.jboss.pnc.auth.keycloakutil.util.HttpUtil.getIdForType;

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
