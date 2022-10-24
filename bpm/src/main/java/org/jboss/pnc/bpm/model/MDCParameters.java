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
package org.jboss.pnc.bpm.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.jboss.pnc.api.constants.MDCKeys;
import org.slf4j.MDC;

@Getter
public class MDCParameters {

    public final String requestContext;

    public final String processContext;

    public final String userId;

    public final String tmp;

    public final String exp;

    public final String buildId;

    public final String traceId;

    public final String spanId;

    @JsonProperty(MDCKeys.REQUEST_TOOK)
    public final String request_took;

    @JsonProperty(MDCKeys.RESPONSE_STATUS)
    public final String response_status;

    public MDCParameters() {
        this.requestContext = MDC.get(MDCKeys.REQUEST_CONTEXT_KEY);
        this.processContext = MDC.get(MDCKeys.PROCESS_CONTEXT_KEY);
        this.userId = MDC.get(MDCKeys.USER_ID_KEY);
        this.tmp = MDC.get(MDCKeys.TMP_KEY);
        this.exp = MDC.get(MDCKeys.EXP_KEY);
        this.buildId = MDC.get(MDCKeys.BUILD_ID_KEY);
        this.request_took = MDC.get(MDCKeys.REQUEST_TOOK);
        this.response_status = MDC.get(MDCKeys.RESPONSE_STATUS);
        this.traceId = MDC.get("trace__id");
        this.spanId = MDC.get("span__id");
    }
}
