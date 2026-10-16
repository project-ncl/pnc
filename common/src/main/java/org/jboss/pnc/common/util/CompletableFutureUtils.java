/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.util;

import java.util.concurrent.CompletableFuture;

/**
 * Utils for completable futures
 */
public class CompletableFutureUtils {

    /**
     * In the presence of exceptions, the original CompletableFuture#allOf waits for all remaining operations to
     * complete. Instead, if we wanted to signal completion as soon as one of the operations complete exceptionally, we
     * would need to change the implementation, provided in this method
     *
     * @param futures list of futures to monitor
     * @param <T> type
     * @return combined completable future
     */
    public static <T> CompletableFuture<T> allOfOrException(CompletableFuture<T>... futures) {
        CompletableFuture<T> failure = new CompletableFuture<>();
        for (CompletableFuture<T> f : futures) {
            f.exceptionally(ex -> {
                failure.completeExceptionally(ex);
                return null;
            });
        }
        return (CompletableFuture<T>) CompletableFuture.anyOf(failure, CompletableFuture.allOf(futures));
    }
}
