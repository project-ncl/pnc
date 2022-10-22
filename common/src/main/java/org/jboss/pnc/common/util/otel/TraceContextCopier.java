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
package org.jboss.pnc.common.util.otel;

import org.jboss.pnc.common.logging.MDCUtils;

import io.opentelemetry.api.trace.Span;

public class TraceContextCopier implements ContextCopier {

    String traceId;
    String spanId;
    String traceFlags;
    String traceState;

    @Override
    public void copy() {
        traceId = Span.current().getSpanContext().getTraceId();
        spanId = Span.current().getSpanContext().getSpanId();
        traceFlags = Span.current().getSpanContext().getTraceFlags().asHex();
        traceState = Span.current().getSpanContext().getTraceState().toString();
    }

    @Override
    public void apply() {
        MDCUtils.addTraceContext(traceId, spanId, traceFlags, traceState);
    }

}
