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
package org.jboss.pnc.common.test.util;

import org.jboss.pnc.common.util.RandomUtils;
import org.jboss.pnc.common.util.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2015-01-01.
 */
public class StringUtilsTest {

    @Test
    public void deserializeInt() throws Exception {
        Integer[] integers = StringUtils.deserializeInt("");
        Assert.assertEquals(0, integers.length);
    }

    @Test
    public void deserializeNullInt() throws Exception {
        Integer[] integers = StringUtils.deserializeInt(null);
        Assert.assertEquals(0, integers.length);
    }

    @Test
    public void serializeInt() throws Exception {
        Integer[] integers = new Integer[0];
        String string = StringUtils.serializeInt(integers);
        Assert.assertEquals(0, string.length());
    }

    @Test
    public void serializeNullInt() throws Exception {
        String string = StringUtils.serializeInt(null);
        Assert.assertEquals(0, string.length());
    }

    @Test
    public void replaceEnvironmentVariableTestCase() {
        String envVariable = getEnvVariable();
        String src = "JAVA_HOME:${env." + envVariable + "}";
        String replaced = StringUtils.replaceEnv(src);
        Assert.assertEquals("JAVA_HOME:" + System.getenv(envVariable), replaced);
    }

    @Test
    public void randomString() {
        assertRandomLengthMatch(0);
        assertRandomLengthMatch(10);
        assertRandomLengthMatch(35);
        assertRandomLengthMatch(36);
        assertRandomLengthMatch(71);
        assertRandomLengthMatch(72);
        assertRandomLengthMatch(100);
        assertRandomLengthMatch(1000);
    }

    private void assertRandomLengthMatch(int len) {
        String randString = RandomUtils.randString(len);
        Assert.assertEquals("Wrong sting length. String:" + randString, len, randString.length());
    }

    private String getEnvVariable() {
        Map<String, String> env = System.getenv();
        return env.keySet().iterator().next();
    }

    @Test
    public void stripEndingSlash() {
        String string = "http://host.com/path";
        Assert.assertEquals(string, StringUtils.stripEndingSlash(string + "/"));
        Assert.assertEquals(string, StringUtils.stripEndingSlash(string));
    }

    @Test
    public void stripTrailingSlash() {
        String string = "path/to";
        Assert.assertEquals(string, StringUtils.stripTrailingSlash("/" + string));
        Assert.assertEquals(string, StringUtils.stripTrailingSlash(string));
    }

    @Test
    public void addEndingSlash() {
        String string = "http://host.com/path";
        Assert.assertEquals(string + "/", StringUtils.addEndingSlash(string));
        Assert.assertEquals(string + "/", StringUtils.addEndingSlash(string + "/"));
        Assert.assertNotEquals(string + "//", StringUtils.addEndingSlash(string + "/"));
    }

    @Test
    public void stripSuffix() {
        Assert.assertEquals("http://host.com/repo", StringUtils.stripSuffix("http://host.com/repo.git", ".git"));

        Assert.assertEquals("http://host.com/repo", StringUtils.stripSuffix("http://host.com/repo", ".git"));

        Assert.assertEquals("", StringUtils.stripSuffix("", ".git"));

        Assert.assertNull(StringUtils.stripSuffix(null, ".git"));
    }

    @Test
    public void stripProtocol() {
        String url = "http://host.com/path";
        Assert.assertEquals("host.com/path", StringUtils.stripProtocol(url));

        url = "https://host.com/path";
        Assert.assertEquals("host.com/path", StringUtils.stripProtocol(url));

        url = "ssh://host.com/path";
        Assert.assertEquals("host.com/path", StringUtils.stripProtocol(url));

        url = "git+ssh://host.com/path.git";
        Assert.assertEquals("host.com/path.git", StringUtils.stripProtocol(url));

        url = "";
        Assert.assertEquals("", StringUtils.stripProtocol(url));

        url = null;
        Assert.assertNull(StringUtils.stripProtocol(url));
    }

    @Test
    public void readStreamShouldNotDropAny() throws IOException {
        String message = "0123456789\n1123456789";
        InputStream is = new ByteArrayInputStream(message.getBytes());
        ArrayDeque<String> lines = new ArrayDeque<>();
        List<String> dropped = new ArrayList<>();
        Consumer<String> droppedLines = dropped::add;
        StringUtils.readStream(is, Charset.defaultCharset(), lines, 29, droppedLines);

        Assert.assertEquals(0, dropped.size());
    }

    @Test
    public void readStreamShouldDropFistLine() throws IOException {
        String message = "0-12345678\n1-12345678\n2-12345678";
        InputStream is = new ByteArrayInputStream(message.getBytes());
        ArrayDeque<String> lines = new ArrayDeque<>();
        List<String> dropped = new ArrayList<>();
        Consumer<String> droppedLines = dropped::add;
        StringUtils.readStream(is, Charset.defaultCharset(), lines, 29, droppedLines);

        Assert.assertEquals(1, dropped.size());
        Assert.assertEquals("0-12345678", dropped.get(0));
    }

    @Test
    public void readStreamShouldDropFistTwoLine() throws IOException {
        String message = "0-12345678\n1-12345678\n2-12345678-a-bit-more";
        InputStream is = new ByteArrayInputStream(message.getBytes());
        ArrayDeque<String> lines = new ArrayDeque<>();
        List<String> dropped = new ArrayList<>();
        Consumer<String> droppedLines = dropped::add;
        StringUtils.readStream(is, Charset.defaultCharset(), lines, 29, droppedLines);

        Assert.assertEquals(2, dropped.size());
        Assert.assertEquals("0-12345678", dropped.get(0));
        Assert.assertEquals("1-12345678", dropped.get(1));
    }
}
