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
package org.jboss.pnc.executor.exceptions;

import org.jboss.pnc.spi.environment.DestroyableEnvironment;

/**
 * Exception in build process, which contains data to clean up after unsuccessful build task
 * 
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
public class BuildProcessException extends ExecutionExceptionWrapper {

    private DestroyableEnvironment destroyableEnvironment;

    public BuildProcessException(String message) {
        super(message);
    }

    public BuildProcessException(Throwable cause) {
        super(cause);
    }

    /**
     * @param cause Exception cause
     * @param destroyableEnvironment Reference to a started environment
     */
    public BuildProcessException(Throwable cause, DestroyableEnvironment destroyableEnvironment) {
        super(cause);
        this.destroyableEnvironment = destroyableEnvironment;
    }

    public DestroyableEnvironment getDestroyableEnvironment() {
        return destroyableEnvironment;
    }

}
