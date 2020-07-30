/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.causewayclient.remotespi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NonNull;

import javax.ws.rs.DefaultValue;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
public class CallbackTarget {
    @NonNull
    private final String url;

    @NonNull
    private final CallbackMethod method;

    private final Map<String, String> headers;

    @JsonCreator
    public CallbackTarget(
            @JsonProperty("url") String url,
            @JsonProperty("method") @DefaultValue("POST") CallbackMethod method,
            @JsonProperty("headers") @DefaultValue("{}") Map<String, String> headers) {
        this.url = url;
        this.method = method;
        this.headers = headers;
    }

    public static CallbackTarget callbackPost(String callBackUrl, String authToken) {
        Map<String, String> callbackHeaders = new HashMap<>();
        callbackHeaders.put("Authorization", "Bearer " + authToken);

        return new CallbackTarget(callBackUrl, CallbackMethod.POST, callbackHeaders);
    }

    public static CallbackTarget callbackPost(String callBackUrl, String authToken, Map<String, String> headers) {
        Map<String, String> callbackHeaders = new HashMap<>();
        callbackHeaders.put("Authorization", "Bearer " + authToken);
        callbackHeaders.putAll(headers);

        return new CallbackTarget(callBackUrl, CallbackMethod.POST, callbackHeaders);
    }
}
