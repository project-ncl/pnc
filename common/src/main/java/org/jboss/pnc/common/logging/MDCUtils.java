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
package org.jboss.pnc.common.logging;

import org.jboss.pnc.api.constants.MDCHeaderKeys;
import org.jboss.pnc.api.constants.MDCKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 * @deprecated use MDCUtils from pnc-common (unless you need Java 8 at compilation)
 */
public class MDCUtils {

    public static final Map<String, String> HEADER_KEY_MAPPING;
    static {
        Map<String, String> mapping = new HashMap<>();
        for (MDCHeaderKeys headerKey : MDCHeaderKeys.values()) {
            mapping.put(headerKey.getMdcKey(), headerKey.getHeaderName());
        }
        HEADER_KEY_MAPPING = Collections.unmodifiableMap(mapping);
    }

    private static final Logger logger = LoggerFactory.getLogger(MDCUtils.class);

    public static void addBuildContext(BuildTaskContext buildTaskContext) {
        addBuildContext(
                buildTaskContext.getBuildContentId(),
                buildTaskContext.isTemporaryBuild(),
                buildTaskContext.getTemporaryBuildExpireDate(),
                buildTaskContext.getUserId());
    }

    public static void addBuildContext(
            String processContext,
            Boolean temporaryBuild,
            Instant temporaryBuildExpireDate,
            String userId) {
        addProcessContext(processContext);
        MDC.put(MDCKeys.TMP_KEY, temporaryBuild.toString());
        MDC.put(MDCKeys.EXP_KEY, temporaryBuildExpireDate.toString());
        if (userId != null) {
            MDC.put(MDCKeys.USER_ID_KEY, userId);
        }
    }

    public static void removeBuildContext() {
        removeProcessContext();
        MDC.remove(MDCKeys.TMP_KEY);
        MDC.remove(MDCKeys.EXP_KEY);
    }

    public static void addProcessContext(String processContext) {
        MDC.put(MDCKeys.PROCESS_CONTEXT_KEY, processContext);
    }

    public static void addUserId(String userId) {
        MDC.put(MDCKeys.USER_ID_KEY, userId);
    }

    public static Optional<String> getRequestContext() {
        return Optional.ofNullable(MDC.get(MDCKeys.REQUEST_CONTEXT_KEY));
    }

    public static Optional<String> getProcessContext() {
        return Optional.ofNullable(MDC.get(MDCKeys.PROCESS_CONTEXT_KEY));
    }

    public static Optional<String> getUserId() {
        return Optional.ofNullable(MDC.get(MDCKeys.USER_ID_KEY));
    }

    public static Optional<String> getTraceId() {
        return Optional.ofNullable(MDC.get(MDCKeys.SLF4J_TRACE_ID_KEY));
    }

    public static Optional<String> getSpanId() {
        return Optional.ofNullable(MDC.get(MDCKeys.SLF4J_SPAN_ID_KEY));
    }

    public static Optional<String> getTraceFlag() {
        return Optional.ofNullable(MDC.get(MDCKeys.SLF4J_TRACE_FLAGS_KEY));
    }

    public static Optional<String> getTraceState() {
        return Optional.ofNullable(MDC.get(MDCKeys.SLF4J_TRACE_STATE_KEY));
    }

    public static void removeProcessContext() {
        MDC.remove(MDCKeys.PROCESS_CONTEXT_KEY);
    }

    public static void addCustomContext(String key, Object value) {
        if (value == null) {
            logger.warn("Setting null for MDC: {}.", key);
            return;
        }
        MDC.put(key, value.toString());
    }

    public static void removeCustomContext(String key) {
        MDC.remove(key);
    }
}
