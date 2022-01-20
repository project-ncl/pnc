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
package org.jboss.pnc.notification;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.jboss.pnc.rest.jackson.JacksonProvider;

import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class RequestParser {

    private final JacksonProvider mapperProvider = new JacksonProvider();

    @Getter
    private String errorMessage;

    @Getter
    private Response.Status failedStatus;

    @Getter
    MessageType messageType;

    private Object dataObject;

    public boolean parseRequest(String message) throws IOException {
        ObjectMapper mapper = mapperProvider.getMapper();

        JsonNode node = null;
        try {
            node = mapper.readTree(message);
        } catch (IOException e) {
            errorMessage = "Cannot parse request massage.";
            failedStatus = Response.Status.BAD_REQUEST;
            throw e;
        }

        if (!node.has("messageType")) {
            errorMessage = "Missing 'messageType' field.";
            failedStatus = Response.Status.BAD_REQUEST;
            return false;
        }

        if (!node.has("data")) {
            errorMessage = "Missing 'data' field.";
            failedStatus = Response.Status.BAD_REQUEST;
            return false;
        }

        try {
            messageType = MessageType.valueOf(node.get("messageType").asText());
        } catch (IllegalArgumentException e) {
            errorMessage = "Invalid message-type: " + messageType + ". Supported types are: "
                    + MessageType.PROCESS_UPDATES;
            failedStatus = Response.Status.NOT_ACCEPTABLE;
            return false;
        }

        try {
            String data = node.get("data").toString();
            dataObject = mapper.readValue(data, messageType.getType());
            return true;
        } catch (IOException e) {
            errorMessage = "Cannot parse data part of request massage.";
            failedStatus = Response.Status.BAD_REQUEST;
            throw e;
        }
    }

    public <T> T getData() {
        return (T) dataObject;
    }
}
