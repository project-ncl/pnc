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
package org.jboss.pnc.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class ProcessStageUtils {

    private static final Logger log = LoggerFactory.getLogger("org.jboss.pnc._userlog_.process-stage-update");
    public static final String MDC_PROCESS_STAGE_NAME = "process_stage_name";
    public static final String MDC_PROCESS_STAGE_STEP = "process_stage_step";

    /**
     * Log the process stage's begin and end stage
     *
     * Note that in the future this might be modified to send logs via Kafka
     *
     * @param processStage process stage name
     * @param step
     * @param message
     */
    private static void logProcessStage(String processStage, Step step, String message) {

        try (MDC.MDCCloseable a = MDC.putCloseable(MDC_PROCESS_STAGE_NAME, processStage);
                MDC.MDCCloseable b = MDC.putCloseable(MDC_PROCESS_STAGE_STEP, step.toString())) {

            log.info("{}", message);
        }
    }

    public static void logProcessStageBegin(String processStage) {
        logProcessStage(processStage, Step.BEGIN, String.format("%s: %s ", Step.BEGIN.toString(), processStage));
    }

    public static void logProcessStageBegin(String processStage, String message) {
        logProcessStage(processStage, Step.BEGIN, message);
    }

    public static void logProcessStageEnd(String processStage) {
        logProcessStage(processStage, Step.END, String.format("%s: %s ", Step.END.toString(), processStage));
    }

    public static void logProcessStageEnd(String processStage, String message) {
        logProcessStage(processStage, Step.END, message);
    }

    public enum Step {
        BEGIN, END
    }
}
