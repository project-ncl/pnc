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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;


public class TraceContextCopier implements ContextCopier {

    private final Logger log = LoggerFactory.getLogger(TraceContextCopier.class);


    String traceId;
    String spanId;
    String traceFlags;
    String traceState;

    @Override
    public void copy() {
        log.debug("TraceContextCopier.copy()...");
        SpanContext spanContext = Span.current().getSpanContext();
        log.debug("TraceContextCopier spanContext: {}", spanContext);
        
        if (!spanContext.equals(SpanContext.getInvalid())) {
            traceId = spanContext.getTraceId();
            spanId = spanContext.getSpanId();
            traceFlags = spanContext.getTraceFlags().asHex();
            traceState = spanContext.getTraceState().toString();
        }
        else {
            log.debug("TraceContextCopier IS INVALID!!!");   
        }
    }

    @Override
    public void apply() {
        MDCUtils.addTraceContext(traceId, spanId, traceFlags, traceState);
    }

}
