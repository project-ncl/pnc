/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.notification;

import org.assertj.core.api.Fail;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

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