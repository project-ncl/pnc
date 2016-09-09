/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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

import java.util.Map;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2015-01-01.
 */
public class StringUtilsTest {

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

}
