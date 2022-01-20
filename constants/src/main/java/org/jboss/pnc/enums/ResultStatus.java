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
package org.jboss.pnc.enums;

/**
 * Status of generic result of some operation or task.
 * 
 * @deprecated use pnc-api
 */
@Deprecated
public enum ResultStatus {
    /**
     * The operation was successful.
     */
    SUCCESS(true),
    /**
     * The operation failed.
     */
    FAILED(false),
    /**
     * The operation timed-out.
     */
    TIMED_OUT(false),
    /**
     * The operation failed unexpectedly.
     */
    SYSTEM_ERROR(false);

    private boolean success;

    ResultStatus(boolean success) {
        this.success = success;
    }

    /**
     * Returns true if the operation resulted is successful.
     */
    public boolean isSuccess() {
        return success;
    }
}
