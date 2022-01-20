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
        CompletableFuture<T> failure = new CompletableFuture<T>();
        for (CompletableFuture<T> f : futures) {
            f.exceptionally(ex -> {
                failure.completeExceptionally(ex);
                return null;
            });
        }
        return (CompletableFuture<T>) CompletableFuture.anyOf(failure, CompletableFuture.allOf(futures));
    }
}
