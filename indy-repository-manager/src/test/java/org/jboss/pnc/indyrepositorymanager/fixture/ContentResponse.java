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
package org.jboss.pnc.indyrepositorymanager.fixture;

import java.io.InputStream;

public final class ContentResponse {
    private final int code;

    private final InputStream bodyStream;

    private final String body;

    private final String path;

    private final String method;

    ContentResponse(final String method, final String path, final int code, final String body) {
        this.method = method;
        this.path = path;
        this.code = code;
        this.body = body;
        this.bodyStream = null;
    }

    ContentResponse(final String method, final String path, final int code, final InputStream bodyStream) {
        this.method = method;
        this.path = path;
        this.code = code;
        this.body = null;
        this.bodyStream = bodyStream;
    }

    public String method() {
        return method;
    }

    public String path() {
        return path;
    }

    public int code() {
        return code;
    }

    public String body() {
        return body;
    }

    public InputStream bodyStream() {
        return bodyStream;
    }

    @Override
    public String toString() {
        return "Expect (" + method + " " + path + "), and respond with code:" + code() + ", body:\n" + body();
    }
}