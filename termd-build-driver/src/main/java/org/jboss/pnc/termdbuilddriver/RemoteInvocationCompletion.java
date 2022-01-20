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
package org.jboss.pnc.termdbuilddriver;

import org.jboss.pnc.buildagent.api.Status;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;

import java.util.Optional;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class RemoteInvocationCompletion {

    private final Status status;

    private final Optional<String> outputChecksum;

    private final BuildDriverException exception;

    /**
     *
     * @param status
     * @param outputChecksum has to be defined for non-interrupted builds.
     */
    public RemoteInvocationCompletion(Status status, Optional<String> outputChecksum) {
        this.status = status;
        this.outputChecksum = outputChecksum;
        this.exception = null;
    }

    public RemoteInvocationCompletion(BuildDriverException exception) {
        this.status = null;
        this.outputChecksum = Optional.empty();
        this.exception = exception;
    }

    public Status getStatus() {
        return status;
    }

    public Optional<String> getOutputChecksum() {
        return outputChecksum;
    }

    public BuildDriverException getException() {
        return exception;
    }
}
