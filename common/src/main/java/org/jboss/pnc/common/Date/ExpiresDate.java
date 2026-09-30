/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.Date;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class ExpiresDate {

    /**
     * @return expiration date. Date is calculated now + temporaryBuildsLifeSpanDays days. If the build is not temporary
     *         returns MAX java date.
     */
    public static Instant getTemporaryBuildExpireDate(long temporaryBuildsLifeSpanDays, boolean isTemporaryBuild) {
        if (isTemporaryBuild) {
            return Instant.now().plus(temporaryBuildsLifeSpanDays, ChronoUnit.DAYS);
        } else {
            return Instant.parse("9999-01-01T00:00:00Z");
        }
    }

}
