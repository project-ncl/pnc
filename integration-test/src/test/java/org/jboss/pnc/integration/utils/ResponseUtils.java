/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.integration.utils;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import io.restassured.response.Response;

public class ResponseUtils {

    public static Integer getIdFromLocationHeader(Response response) {
        String location = response.getHeader("Location");
        return Integer.valueOf(location.substring(location.lastIndexOf("/") + 1));
    }

    public static void waitSynchronouslyFor(Supplier<Boolean> condition, long timeout, TimeUnit timeUnit) {
        long stopTime = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(timeout, timeUnit);
        do {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new AssertionError("Unexpected interruption", e);
            }
            if (System.currentTimeMillis() > stopTime) {
                throw new AssertionError(
                        "Timeout " + timeout + " " + timeUnit + " reached while waiting for condition");
            }
        } while (!condition.get());
    }
}
