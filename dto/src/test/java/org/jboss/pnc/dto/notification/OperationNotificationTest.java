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
package org.jboss.pnc.dto.notification;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.assertj.core.api.Fail;
import org.junit.Test;

public class OperationNotificationTest {

    // same creation as in VertxWebSocketClient
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS);

    @Test
    public void testDeserialization() {
        String jsonOperationNotification = "{" + "\"job\":\"OPERATION\"," + "\"notificationType\":\"BUILD_PUSH\","
                + "\"progress\":\"FINISHED\"," + "\"oldProgress\":\"IN_PROGRESS\"," + "\"message\":null,"
                + "\"operationId\":\"1234\"," + "\"result\":\"SUCCESSFUL\"," + "\"operation\":null" + "}";

        try {
            OBJECT_MAPPER.readValue(jsonOperationNotification, OperationNotification.class);
        } catch (Exception e) {
            Fail.fail("Cannot parse string to OperationNotification", e);
        }
    }
}