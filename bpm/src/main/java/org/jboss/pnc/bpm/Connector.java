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
package org.jboss.pnc.bpm;

import org.jboss.pnc.spi.exception.ProcessManagerException;

import io.opentelemetry.instrumentation.annotations.WithSpan;

import java.io.Closeable;

/**
 * @author Matej Lazar
 */
public interface Connector extends Closeable {

    /**
     * @deprecated start the process with correlationKey
     */
    @Deprecated
    Long startProcess(String processId, Object processParameters, String accessToken) throws ProcessManagerException;

    @WithSpan()
    Long startProcess(String processId, Object requestObject, String correlationKey, String accessToken)
            throws ProcessManagerException;

    /**
     * Use only for a scheduled cleanup
     */
    @Deprecated
    boolean isProcessInstanceCompleted(Long processInstanceId);

    boolean cancelByCorrelation(String correlationKey, String accessToken);

    /**
     * @deprecated use cancelByCorrelation
     */
    @Deprecated
    boolean cancel(Long processInstanceId, String accessToken);

    @Override
    void close();

}
