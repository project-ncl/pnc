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

package org.jboss.pnc.common.util;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class TimeUtils {

    /**
     * Converts a {@code TimeUnit} to a {@code ChronoUnit}.
     * <p>
     * This handles the seven units declared in {@code TimeUnit}.
     *
     * @param unit the unit to convert, not null
     * @return the converted unit, not null
     */
    public static ChronoUnit chronoUnit(TimeUnit unit) {
        Objects.requireNonNull(unit, "unit");
        switch (unit) {
            case NANOSECONDS:
                return ChronoUnit.NANOS;
            case MICROSECONDS:
                return ChronoUnit.MICROS;
            case MILLISECONDS:
                return ChronoUnit.MILLIS;
            case SECONDS:
                return ChronoUnit.SECONDS;
            case MINUTES:
                return ChronoUnit.MINUTES;
            case HOURS:
                return ChronoUnit.HOURS;
            case DAYS:
                return ChronoUnit.DAYS;
            default:
                throw new IllegalArgumentException("Unknown TimeUnit constant");
        }
    }

    /**
     * Converts a {@code ChronoUnit} to a {@code TimeUnit}.
     * <p>
     * This handles the seven units declared in {@code TimeUnit}.
     *
     * @param unit the unit to convert, not null
     * @return the converted unit, not null
     * @throws IllegalArgumentException if the unit cannot be converted
     */
    public static TimeUnit timeUnit(ChronoUnit unit) {
        Objects.requireNonNull(unit, "unit");
        switch (unit) {
            case NANOS:
                return TimeUnit.NANOSECONDS;
            case MICROS:
                return TimeUnit.MICROSECONDS;
            case MILLIS:
                return TimeUnit.MILLISECONDS;
            case SECONDS:
                return TimeUnit.SECONDS;
            case MINUTES:
                return TimeUnit.MINUTES;
            case HOURS:
                return TimeUnit.HOURS;
            case DAYS:
                return TimeUnit.DAYS;
            default:
                throw new IllegalArgumentException("ChronoUnit cannot be converted to TimeUnit: " + unit);
        }
    }

    /**
     * Generate timestamp in the format tYYYYMMDD-HHMMSS-XXX (XXX = miliseconds)
     *
     * @param generationEnabled Flag indicating, if the generation of timestamp is enabled
     * @param dateInstant Time instant for which the timestamp will be generated
     * @return Timestamp string or null, if generation is disabled
     */
    public static String generateTimestamp(boolean generationEnabled, Date dateInstant) {
        if (!generationEnabled)
            return null;

        Calendar instant = new Calendar.Builder().setInstant(dateInstant)
                .setTimeZone(TimeZone.getTimeZone("UTC"))
                .build();

        return String.format(
                "t%d%02d%02d-%02d%02d%02d-%03d",
                instant.get(Calendar.YEAR),
                instant.get(Calendar.MONTH) + 1,
                instant.get(Calendar.DAY_OF_MONTH),
                instant.get(Calendar.HOUR_OF_DAY),
                instant.get(Calendar.MINUTE),
                instant.get(Calendar.SECOND),
                instant.get(Calendar.MILLISECOND));
    }

    /**
     * Get date object representing timestamp X days ago specified by parameter
     *
     * @param numberOfDays Number of days to shift the time back
     * @return New Date object representing a timestamp X days before now
     */
    public static Date getDateXDaysAgo(int numberOfDays) {
        return new Date(Instant.now().minus(numberOfDays, ChronoUnit.DAYS).toEpochMilli());
    }

    public static Instant toInstant(Date date) {
        if (date == null) {
            return null;
        } else {
            return date.toInstant();
        }
    }
}
