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
package org.jboss.pnc.constants;

/**
 * This class provides keys for Mapped Diagnostic Context (MDC) in logging.
 *
 * @deprecated use pnc-api
 */
@Deprecated
public class MDCKeys {
    /**
     * Identifier of the original request context.
     */
    public static final String REQUEST_CONTEXT_KEY = "requestContext";
    /**
     * Identifier of a running process.
     */
    public static final String PROCESS_CONTEXT_KEY = "processContext";
    /**
     * Identifier of user who initiated the operation.
     */
    public static final String USER_ID_KEY = "userId";
    /**
     * Indicator if the context belongs to temporary build.
     *
     * <p>
     * Value: "true" or "false"
     * </p>
     */
    public static final String TMP_KEY = "tmp";
    /**
     * When the log can expire and may be deleted.
     *
     * <p>
     * Value: DateTimeFormatter.ISO_INSTANT.format(timeOfExpiration)
     * </p>
     */
    public static final String EXP_KEY = "exp";
    /**
     * Identifier of the build the operation is working with.
     */
    public static final String BUILD_ID_KEY = "buildId";

    /**
     * This is the ID of the whole trace forest and is used to uniquely identify a distributed trace through a system.
     * <p>
     * Value: 16-byte array
     * </p>
     */
    public static final String TRACE_ID_KEY = "traceId";

    /**
     * This is the ID of this request as known by the caller (in some tracing systems, this is known as the span-id,
     * where a span is the execution of a client request).
     * <p>
     * Value: 8-byte array
     * </p>
     */
    public static final String SPAN_ID_KEY = "spanId";

    /**
     * This is the ID of this request as known by the caller (in some tracing systems, this is known as the span-id,
     * where a span is the execution of a client request).
     * <p>
     * Value: 8-byte array
     * </p>
     */
    public static final String PARENT_ID_KEY = "parentId";
}
