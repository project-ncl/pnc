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
package org.jboss.pnc.rest.restmodel.response.error;

import org.jboss.pnc.rest.validation.exceptions.RestValidationException;
import org.jboss.pnc.spi.exception.BuildConflictException;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ErrorResponseRest {

    private String errorType;
    private String errorMessage;
    private Object details;

    public ErrorResponseRest() {
    }

    public ErrorResponseRest(String errorType, String errorMessage) {
        this.errorType = errorType;
        this.errorMessage = errorMessage;
    }

    public ErrorResponseRest(Exception e) {
        this.errorType = e.getClass().getSimpleName();
        this.errorMessage = e.getMessage();
        this.details = null;
    }

    public ErrorResponseRest(RestValidationException e) {
        this.errorType = e.getClass().getSimpleName();
        this.errorMessage = e.getMessage();
        this.details = e.getRestModelForException().orElse(null);
    }

    public ErrorResponseRest(BuildConflictException e) {
        this.errorType = e.getClass().getSimpleName();
        this.errorMessage = e.getMessage() + ": " + e.getBuildTaskId();
        this.details = null;
    }

    public String getErrorType() {
        return errorType;
    }

    public Object getDetails() {
        return details;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
