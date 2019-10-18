/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.common.util;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class BuildStageUtils {

    public static final String MDC_BUILD_STAGE = "build_stage";
    public static final String MDC_BUILD_STAGE_DURATION = "build_stage_duration";

    /**
     * Log the build stage's duration.
     *
     * Note that in the future this might be modified to send logs via Kafka
     *
     * @param buildStage build stage who's duration we're logging
     * @param duration in seconds
     */
    public static void logBuildStage(String buildStage, int duration) {

        Map<String, String> mdc = MDC.getCopyOfContextMap();
        if (mdc == null) {
            mdc = new HashMap<>();
        }
        mdc.put(MDC_BUILD_STAGE, buildStage);
        mdc.put(MDC_BUILD_STAGE_DURATION, String.valueOf(duration));
        MDC.setContextMap(mdc);

        log.info("END: {}, took: {} seconds", buildStage, duration);
    }
}
