/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.test.util;

import java.time.LocalDateTime;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class Wait {

    public static void forCondition(Supplier<Boolean> evaluationSupplier, long timeout, TemporalUnit timeUnit)
            throws InterruptedException, TimeoutException {
        forCondition(evaluationSupplier, timeout, timeUnit, "");
    }

    public static void forCondition(
            Supplier<Boolean> evaluationSupplier,
            long timeout,
            TemporalUnit timeUnit,
            String failedMessage) throws InterruptedException, TimeoutException {
        forCondition(evaluationSupplier, timeout, timeUnit, () -> failedMessage);
    }

    public static void forCondition(
            Supplier<Boolean> evaluationSupplier,
            long timeout,
            TemporalUnit timeUnit,
            Supplier<String> failedMessageProvider) throws InterruptedException, TimeoutException {
        LocalDateTime started = LocalDateTime.now();
        do {
            Thread.sleep(50);
            if (started.plus(timeout, timeUnit).isBefore(LocalDateTime.now())) {
                throw new TimeoutException(
                        failedMessageProvider.get() + " Reached timeout " + timeout + " " + timeUnit);
            }
        } while (!evaluationSupplier.get());
    }

}
