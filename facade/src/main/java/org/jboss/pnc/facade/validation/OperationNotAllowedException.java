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
package org.jboss.pnc.facade.validation;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class OperationNotAllowedException extends RuntimeException {

    private Object responseObject;

    public OperationNotAllowedException(String message) {
        super(message);
    }

    public OperationNotAllowedException(String message, Object o) {
        super(message);
        this.responseObject = o;
    }

    public OperationNotAllowedException(String message, Throwable cause, Object o) {
        super(message, cause);
    }

    public OperationNotAllowedException(Throwable cause, Object o) {
        super(cause);
        this.responseObject = o;
    }

    public OperationNotAllowedException(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace,
            Object o) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.responseObject = o;
    }

    public OperationNotAllowedException(Object o) {
        super();
        this.responseObject = o;
    }

    public Object getResponseObject() {
        return responseObject;
    }
}
