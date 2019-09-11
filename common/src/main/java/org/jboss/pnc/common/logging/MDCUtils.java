/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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

    private static final String REQUEST_CONTEXT_KEY = "requestContext";
    private static final String PROCESS_CONTEXT_KEY = "processContext";
    private static final String USER_ID_KEY = "userId";
    private static final String TMP_KEY = "tmp";;
    private static final String EXP_KEY = "exp";

    public static void addContext(BuildTaskContext buildTaskContext) {
        addBuildContext(
                buildTaskContext.getBuildContentId(),
                buildTaskContext.isTemporaryBuild(),
                buildTaskContext.getTemporaryBuildExpireDate()
        );
    }

    public static void addBuildContext(String processContext, Boolean temporaryBuild, Instant temporaryBuildExpireDate) {
        Map<String, String> context = getContextMap();
        addProcessContext(processContext);
        context.put(TMP_KEY, temporaryBuild.toString());
        context.put(EXP_KEY, temporaryBuildExpireDate.toString());
        MDC.setContextMap(context);
    }

    public static void addProcessContext(String processContext) {
        Map<String, String> context = getContextMap();
        String current = context.get(PROCESS_CONTEXT_KEY);
        if (StringUtils.isEmpty(current)) {
            context.put(PROCESS_CONTEXT_KEY, processContext);
            MDC.setContextMap(context);
        } else {
            logger.warn("Did not set new processContext [{}] as value already exists [{}].", processContext, current);
        }
    }

    public static void addRequestContext(String requestContext) {
        Map<String, String> context = getContextMap();
        context.put(REQUEST_CONTEXT_KEY, requestContext);
        MDC.setContextMap(context);
    }

    public static void addUserId(String userId) {
        Map<String, String> context = getContextMap();
        context.put(USER_ID_KEY, userId);
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
        return Optional.ofNullable(getContextMap().get(REQUEST_CONTEXT_KEY));
    }

    public static Optional<String> getProcessContext() {
        return Optional.ofNullable(getContextMap().get(PROCESS_CONTEXT_KEY));
    }

    public static Optional<String> getUserId() {
        return Optional.ofNullable(getContextMap().get(USER_ID_KEY));
    }

    public static void clear() {
        MDC.clear();
    }

    public static void removeProcessContext() {
        MDC.remove(PROCESS_CONTEXT_KEY);
    }

    public static Map<String, String> getMDCToHeaderMappings() {
        Map<String, String> mappings = new HashMap<>();
        mappings.put(USER_ID_KEY, "log-user-id");
        mappings.put(REQUEST_CONTEXT_KEY, "log-request-context");
        mappings.put(PROCESS_CONTEXT_KEY, "log-process-context");
        mappings.put(TMP_KEY, "log-tmp");
        mappings.put(EXP_KEY, "log-exp");
        return mappings;
    }
}
