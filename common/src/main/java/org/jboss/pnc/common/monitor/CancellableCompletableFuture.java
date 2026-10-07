/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.monitor;

import java.util.concurrent.CompletableFuture;

/**
 * CompletableFuture with task cancellation support.
 *
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class CancellableCompletableFuture<T> extends CompletableFuture<T> {

    private final Runnable onCancel;

    /**
     * OnCancel {@link Runnable} is invoked when the {@link CancellableCompletableFuture#cancel(boolean)} is called.
     *
     * @param onCancel
     */
    public CancellableCompletableFuture(Runnable onCancel) {
        this.onCancel = onCancel;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        onCancel.run();
        return super.cancel(mayInterruptIfRunning);
    }
}
