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
package org.jboss.pnc.common.test.util;

import org.jboss.pnc.common.util.TimeUtils;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Jakub Bartecek
 */
public class TimeUtilsTest {

    @Test
    public void generateTimestampDisabledTest() {
        String timestamp = TimeUtils.generateTimestamp(false, new Date());
        assertNull(timestamp);
    }

    @Test
    public void generateTimestampValidTest() {
        // given
        Date date = new Date(1512050025464L); // Nov 30, 2017 1:53:45 PM (2017-11-30-13:53:45) UTC (464 milliseconds)

        // when
        String timestamp = TimeUtils.generateTimestamp(true, date);

        // then
        assertEquals("t20171130-135345-464", timestamp);
    }

    @Test
    public void generateCorrectTimeStampFormatForSingleDigitDates() {
        // given
        // test that TimeUtils appends an extra '0' when month, day, or time is a single digit
        Date date = new Date(1501722185009L); // Aug 03, 2017 1:03:05 AM (2017-08-03-01:03:05) UTC (9 milliseconds)

        // when
        String timestamp = TimeUtils.generateTimestamp(true, date);

        // then
        assertEquals("t20170803-010305-009", timestamp);
    }

    @Test
    public void getDateXDaysAgoBeforeNowTest() {
        // given
        Date now = new Date();

        // when
        Date date = TimeUtils.getDateXDaysAgo(14);

        // then
        assertTrue(date.before(now));
    }
}
