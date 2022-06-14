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

import org.jboss.pnc.api.constants.MDCKeys;
import org.jboss.pnc.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class MDCUtils {

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
        Map<String, String> context = getContextMap();
        addProcessContext(processContext, context);
        context.put(MDCKeys.USER_ID_KEY, userId);
        context.put(MDCKeys.TMP_KEY, temporaryBuild.toString());
        context.put(MDCKeys.EXP_KEY, temporaryBuildExpireDate.toString());
        MDC.setContextMap(context);
    }

    public static void removeBuildContext() {
        removeProcessContext();
        MDC.remove(MDCKeys.USER_ID_KEY);
        MDC.remove(MDCKeys.TMP_KEY);
        MDC.remove(MDCKeys.EXP_KEY);
    }

    public static void addProcessContext(String processContext) {
        Map<String, String> context = getContextMap();
        addProcessContext(processContext, context);
        MDC.setContextMap(context);
    }

    private static void addProcessContext(String processContext, Map<String, String> map) {
        String current = map.get(MDCKeys.PROCESS_CONTEXT_KEY);
        if (StringUtils.isEmpty(current)) {
            map.put(MDCKeys.PROCESS_CONTEXT_KEY, processContext);
        } else {
            logger.warn("Did not set new processContext [{}] as value already exists [{}].", processContext, current);
        }
    }

    public static void addRequestContext(String requestContext) {
        Map<String, String> context = getContextMap();
        context.put(MDCKeys.REQUEST_CONTEXT_KEY, requestContext);
        MDC.setContextMap(context);
    }

    public static void addUserId(String userId) {
        Map<String, String> context = getContextMap();
        context.put(MDCKeys.USER_ID_KEY, userId);
        MDC.setContextMap(context);
    }

    private static Map<String, String> getContextMap() {
        Map<String, String> context = MDC.getCopyOfContextMap();
        if (context == null) {
            context = new HashMap<>();
        }
        return context;
    }

    public static Optional<String> getRequestContext() {
        return Optional.ofNullable(getContextMap().get(MDCKeys.REQUEST_CONTEXT_KEY));
    }

    public static Optional<String> getProcessContext() {
        return Optional.ofNullable(getContextMap().get(MDCKeys.PROCESS_CONTEXT_KEY));
    }

    public static Optional<String> getUserId() {
        return Optional.ofNullable(getContextMap().get(MDCKeys.USER_ID_KEY));
    }

    public static Optional<String> getCustomContext(String key) {
        return Optional.ofNullable(getContextMap().get(key));
    }

    public static void clear() {
        MDC.clear();
    }

    public static void removeProcessContext() {
        MDC.remove(MDCKeys.PROCESS_CONTEXT_KEY);
    }

    public static Map<String, String> getMDCToHeaderMappings() {
        Map<String, String> mappings = new HashMap<>();
        mappings.put(MDCKeys.USER_ID_KEY, "log-user-id");
        mappings.put(MDCKeys.REQUEST_CONTEXT_KEY, "log-request-context");
        mappings.put(MDCKeys.PROCESS_CONTEXT_KEY, "log-process-context");
        mappings.put(MDCKeys.TMP_KEY, "log-tmp");
        mappings.put(MDCKeys.EXP_KEY, "log-exp");
        return mappings;
    }

    public static Map<String, String> getMdcAsHeadersMap() {
        Map<String, String> headers = new HashMap<>();
        Map<String, String> mdcContextMap = getContextMap();
        getMDCToHeaderMappings().forEach((mdcName, headerName) -> {
            String mdcEntry = mdcContextMap.get(mdcName);
            if (!StringUtils.isEmpty(mdcEntry)) {
                headers.put(headerName, mdcEntry);
            }
        });
        return headers;
    }

    public static void addCustomContext(String key, String value) {
        Map<String, String> context = getContextMap();
        context.put(key, value);
        MDC.setContextMap(context);
    }

    public static void removeCustomContext(String key) {
        MDC.remove(key);
    }
}
