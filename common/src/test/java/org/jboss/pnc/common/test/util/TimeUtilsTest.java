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

import org.jboss.pnc.common.util.TimeUtils;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * @author Jakub Bartecek
 */
public class TimeUtilsTest {

    @Test
    public void generateTimestampDisabledTest() {
        String timestamp = TimeUtils.generateTimestamp(false, new Date());
        assertEquals(null, timestamp);
    }

    @Test
    public void generateTimestampValidTest() {
        // given
        Date date = new Date(1512050025464L); //Nov 30, 2017 2:53:45 PM (2017-11-30-14:53:45)

        //when
        String timestamp = TimeUtils.generateTimestamp(true, date);

        //then
        assertEquals("t20171130-135345-464", timestamp);
    }
}
