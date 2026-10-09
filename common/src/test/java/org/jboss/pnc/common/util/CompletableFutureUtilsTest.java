/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import org.junit.Test;

public class CompletableFutureUtilsTest {

    @Test
    public void testCompletableFutureSuccessful() {

        CompletableFuture<String> first = CompletableFuture.supplyAsync(() -> "first");
        CompletableFuture<String> second = CompletableFuture.supplyAsync(() -> "second");
        CompletableFuture<String> third = CompletableFuture.supplyAsync(() -> "third");

        CompletableFuture<String> all = CompletableFutureUtils.allOfOrException(first, second, third);
        waitForTerminationAndIgnoreException(all);

        assertThat(all.isCompletedExceptionally()).as("future should not complete exceptionally").isFalse();
    }

    @Test
    public void testCompletableFutureFailed() {

        CompletableFuture<String> first = CompletableFuture.supplyAsync(() -> "first");
        CompletableFuture<String> second = CompletableFuture.supplyAsync(() -> "second");
        CompletableFuture<String> third = CompletableFuture.supplyAsync(() -> {
            throw new RuntimeException();
        });
        CompletableFuture<String> all = CompletableFutureUtils.allOfOrException(first, second, third);
        waitForTerminationAndIgnoreException(all);

        assertThat(all.isCompletedExceptionally()).as("future should complete exceptionally").isTrue();
    }

    /**
     * Don't wait for all futures to end before signalling an exception is thrown.
     */
    @Test
    public void testCompletableFutureTerminateOnFirstException() {

        Instant start = Instant.now();

        CompletableFuture<String> first = CompletableFuture.supplyAsync(() -> {
            return "first";
        });

        // second finishes after 10 seconds
        CompletableFuture<String> second = CompletableFuture.supplyAsync(() -> {
            sleep(10000);
            return "second";
        });

        // third throws an exception after 0.5 seconds
        CompletableFuture<String> third = CompletableFuture.supplyAsync(() -> {
            sleep(500);
            throw new RuntimeException();
        });

        CompletableFuture<String> all = CompletableFutureUtils.allOfOrException(first, second, third);
        waitForTerminationAndIgnoreException(all);

        long elapsed = Duration.between(start, Instant.now()).toMillis();

        assertThat(all.isCompletedExceptionally()).as("future should complete exceptionally").isTrue();

        // The time elapsed between future running and the combined completable future failing must be greater or
        // to 0.5 seconds (third future failing), but before 10 seconds (the second future finishing)
        assertThat(elapsed).as("Time elapsed must be greater or equal to 0.5s, but less than 10s")
                .isGreaterThanOrEqualTo(500)
                .isLessThan(10000);
    }

    @Test
    public void testWaitForAllSuccessfulFuturesAreDone() {
        Instant start = Instant.now();

        CompletableFuture<String> first = CompletableFuture.supplyAsync(() -> {
            return "first";
        });

        // second finishes after 2 seconds
        CompletableFuture<String> second = CompletableFuture.supplyAsync(() -> {
            sleep(2000);
            return "second";
        });

        // third throws an exception after 0.5 seconds
        CompletableFuture<String> third = CompletableFuture.supplyAsync(() -> {
            sleep(500);
            return "third";
        });

        CompletableFuture<String> all = CompletableFutureUtils.allOfOrException(first, second, third);
        waitForTerminationAndIgnoreException(all);

        long elapsed = Duration.between(start, Instant.now()).toMillis();

        assertThat(all.isCompletedExceptionally()).as("future did not complete exceptionally").isFalse();

        // The time elapsed between future running and the combined completable future should be greater or equal to 2
        // seconds
        assertThat(elapsed).as("Time elapsed must be greater or equal 2 seconds").isGreaterThanOrEqualTo(2000);
    }

    private <T> void waitForTerminationAndIgnoreException(CompletableFuture<T> completableFuture) {
        try {
            completableFuture.join();
        } catch (Exception e) {
        }
    }

    private void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}