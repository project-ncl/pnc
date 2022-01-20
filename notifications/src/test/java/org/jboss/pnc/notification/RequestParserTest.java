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

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class RequestParserTest {

    @Test
    public void shouldParseRequest() throws IOException {
        String message = "{ \"messageType\": \"PROCESS_UPDATES\", \"data\": { \"action\": \"SUBSCRIBE\", \"topic\": \"component-build\", \"id\":\"123\" } }";
        RequestParser requestParser = new RequestParser();
        boolean parsed = requestParser.parseRequest(message);

        Assert.assertTrue("Parsing failed.", parsed);

        MessageType messageType = requestParser.getMessageType();
        if (MessageType.PROCESS_UPDATES.equals(messageType)) {
            ProgressUpdatesRequest progressUpdatesRequest = requestParser.<ProgressUpdatesRequest> getData();
            Assert.assertEquals("123", progressUpdatesRequest.getId());
        }
    }
}
