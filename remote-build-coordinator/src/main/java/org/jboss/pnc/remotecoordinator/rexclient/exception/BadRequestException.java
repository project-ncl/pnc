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
package org.jboss.pnc.remotecoordinator.rexclient.exception;

/**
 * HTTP Request returned 404 Bad Request
 */
public class BadRequestException extends RuntimeException {

    private final String detail;

    public BadRequestException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    public BadRequestException(String message, Throwable cause, String detail) {
        super(message, cause);
        this.detail = detail;
    }

    public BadRequestException(Throwable cause, String detail) {
        super(cause);
        this.detail = detail;
    }
}
