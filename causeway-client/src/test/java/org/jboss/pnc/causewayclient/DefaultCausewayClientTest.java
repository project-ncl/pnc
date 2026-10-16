/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.causewayclient;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

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
