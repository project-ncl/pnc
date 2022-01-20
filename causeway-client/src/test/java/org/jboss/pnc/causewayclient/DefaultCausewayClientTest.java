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
package org.jboss.pnc.causewayclient;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;

public class DefaultCausewayClientTest {

    @Test
    public void secureBodyLogTest() throws IOException {
        // given
        ClassLoader classLoader = getClass().getClassLoader();
        String originalJsonMessage = FileUtils.readFileToString(
                new File(classLoader.getResource("bodyJsonMessage.json").getFile()),
                Charset.forName("UTF-8"));
        String expectedSecuredJson = FileUtils.readFileToString(
                new File(classLoader.getResource("bodyJsonMessageSecure.json").getFile()),
                Charset.forName("UTF-8"));

        // when
        String securedJson = new DefaultCausewayClient().secureBodyLog(originalJsonMessage);

        // then
        assertEquals(expectedSecuredJson, securedJson);
    }
}
