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
 * Rex returned Constraint validation on Build ID. A Task with the same Build ID already exists.
 */
public class BuildIDConflictException extends ConflictResponseException {
    private final String buildId;

    public BuildIDConflictException(String buildId) {
        this.buildId = buildId;
    }

    public BuildIDConflictException(String message, String buildId) {
        super(message);
        this.buildId = buildId;
    }

    public BuildIDConflictException(String message, Throwable cause, String buildId) {
        super(message, cause);
        this.buildId = buildId;
    }

    public BuildIDConflictException(Throwable cause, String buildId) {
        super(cause);
        this.buildId = buildId;
    }
}
