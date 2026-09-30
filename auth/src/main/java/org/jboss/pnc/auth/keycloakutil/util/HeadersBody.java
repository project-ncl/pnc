/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.auth.keycloakutil.util;

import static org.jboss.pnc.auth.keycloakutil.util.IoUtil.copyStream;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class HeadersBody {

    private Headers headers;
    private InputStream body;

    public HeadersBody(Headers headers) {
        this.headers = headers;
    }

    public HeadersBody(Headers headers, InputStream body) {
        this.headers = headers;
        this.body = body;
    }

    public Headers getHeaders() {
        return headers;
    }

    public InputStream getBody() {
        return body;
    }

    public String readBodyString() {
        byte[] buffer = readBodyBytes();
        return new String(buffer, Charset.forName(getContentCharset()));
    }

    public byte[] readBodyBytes() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        copyStream(getBody(), os);
        return os.toByteArray();
    }

    public String getContentCharset() {
        Header contentType = headers.get("Content-Type");
        if (contentType != null) {
            int pos = contentType.getValue().lastIndexOf("charset=");
            if (pos != -1) {
                return contentType.getValue().substring(pos + 8);
            }
        }
        return "iso-8859-1";
    }
}
