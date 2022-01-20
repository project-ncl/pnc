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
package org.jboss.pnc.auth.keycloakutil.operations;

import org.jboss.pnc.auth.keycloakutil.util.Headers;
import org.jboss.pnc.auth.keycloakutil.util.HeadersBody;
import org.jboss.pnc.auth.keycloakutil.util.HeadersBodyStatus;
import org.jboss.pnc.auth.keycloakutil.util.HttpUtil;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.util.JsonSerialization;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static org.jboss.pnc.auth.keycloakutil.util.HttpUtil.composeResourceUrl;
import static org.jboss.pnc.auth.keycloakutil.util.HttpUtil.doDeleteJSON;
import static org.jboss.pnc.auth.keycloakutil.util.HttpUtil.doPostJSON;
import static org.jboss.pnc.auth.keycloakutil.util.HttpUtil.getIdForType;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class UserOperations {

    public static void addRealmRoles(String rootUrl, String realm, String auth, String userid, List<?> roles) {
        String resourceUrl = composeResourceUrl(rootUrl, realm, "users/" + userid + "/role-mappings/realm");
        doPostJSON(resourceUrl, auth, roles);
    }

    public static void addClientRoles(
            String rootUrl,
            String realm,
            String auth,
            String userid,
            String idOfClient,
            List<?> roles) {
        String resourceUrl = composeResourceUrl(
                rootUrl,
                realm,
                "users/" + userid + "/role-mappings/clients/" + idOfClient);
        doPostJSON(resourceUrl, auth, roles);
    }

    public static void removeRealmRoles(String rootUrl, String realm, String auth, String userid, List<?> roles) {
        String resourceUrl = composeResourceUrl(rootUrl, realm, "users/" + userid + "/role-mappings/realm");
        doDeleteJSON(resourceUrl, auth, roles);
    }

    public static void removeClientRoles(
            String rootUrl,
            String realm,
            String auth,
            String userid,
            String idOfClient,
            List<?> roles) {
        String resourceUrl = composeResourceUrl(
                rootUrl,
                realm,
                "users/" + userid + "/role-mappings/clients/" + idOfClient);
        doDeleteJSON(resourceUrl, auth, roles);
    }

    public static void resetUserPassword(
            String rootUrl,
            String realm,
            String auth,
            String userid,
            String password,
            boolean temporary) {

        String resourceUrl = composeResourceUrl(rootUrl, realm, "users/" + userid + "/reset-password");

        Headers headers = new Headers();
        if (auth != null) {
            headers.add("Authorization", auth);
        }
        headers.add("Content-Type", "application/json");

        CredentialRepresentation credentials = new CredentialRepresentation();
        credentials.setType("password");
        credentials.setTemporary(temporary);
        credentials.setValue(password);

        HeadersBodyStatus response;

        byte[] body;
        try {
            body = JsonSerialization.writeValueAsBytes(credentials);
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize JSON", e);
        }

        try {
            response = HttpUtil.doRequest("put", resourceUrl, new HeadersBody(headers, new ByteArrayInputStream(body)));
        } catch (IOException e) {
            throw new RuntimeException("HTTP request failed: PUT " + resourceUrl + "\n" + new String(body), e);
        }

        response.checkSuccess();
    }

    public static String getIdFromUsername(String rootUrl, String realm, String auth, String username) {
        return getIdForType(rootUrl, realm, auth, "users", "username", username);
    }
}
