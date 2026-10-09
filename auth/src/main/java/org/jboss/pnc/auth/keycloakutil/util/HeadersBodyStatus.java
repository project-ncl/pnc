/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.auth.keycloakutil.util;

import java.io.InputStream;
import java.util.Map;

import org.keycloak.util.JsonSerialization;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class HeadersBodyStatus extends HeadersBody {

    private final String status;

    public HeadersBodyStatus(String status, Headers headers, InputStream body) {
        super(headers, body);
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    private String getStatusCodeAndReason() {
        return getStatus().substring(9);
    }

    public void checkSuccess() {
        int code = getStatusCode();
        if (code < 200 || code >= 300) {
            String content = readBodyString();
            Map<String, String> error = null;
            try {
                error = JsonSerialization.readValue(content, Map.class);
            } catch (Exception ignored) {
            }

            String message = null;
            if (error != null) {
                String description = error.get("error_description");
                String err = error.get("error");
                String msg = error.get("errorMessage");
                message = msg != null ? msg : err != null ? (description + " [" + error.get("error") + "]") : null;
            }
            throw new HttpResponseException(getStatusCodeAndReason(), message, new RuntimeException(content));
        }
    }

    public int getStatusCode() {
        return Integer.valueOf(status.split(" ")[1]);
    }
}
