/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class TimeUtilsTest {

    @Test
    public void testGetDateXDaysAgo() {

        Date oldDate = TimeUtils.getDateXDaysAgo(200);
        Date current = new Date();
        assertThat(oldDate.before(current)).isTrue();

        long diffInMillies = Math.abs(current.getTime() - oldDate.getTime());
        long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        assertThat(diff).isEqualTo(200L);
    }
}
