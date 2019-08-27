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

import org.slf4j.MDC;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class MDCUtils {

    public static void addContext(BuildTaskContext buildTaskContext) {
        addBuildContext(
                buildTaskContext.getBuildContentId(),
                buildTaskContext.isTemporaryBuild(),
                buildTaskContext.getTemporaryBuildExpireDate()
        );
    }

    public static void addBuildContext(String buildContentId, Boolean temporaryBuild, Instant temporaryBuildExpireDate) {
        Map<String, String> context = getContextMap();
        context.put("processContext", buildContentId);
        context.put("tmp", temporaryBuild.toString());
        context.put("exp", temporaryBuildExpireDate.toString());
        MDC.setContextMap(context);
    }

    public static void addRequestContext(String requestContext) {
        Map<String, String> context = getContextMap();
        context.put("requestContext", requestContext);
        MDC.setContextMap(context);
    }

    private static Map<String, String> getContextMap() {
        Map<String, String> context = MDC.getCopyOfContextMap();
        if (context == null) {
            context = new HashMap<>();
        }
        return context;
    }

    public static void clear() {
        MDC.clear();
    }
}
