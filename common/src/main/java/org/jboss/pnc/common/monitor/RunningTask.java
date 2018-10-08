/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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

package org.jboss.pnc.common.monitor;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
class RunningTask {

    private ScheduledFuture<?> future;
    Instant deadline;
    Consumer<RunningTask> onTimeout;

    public RunningTask(ScheduledFuture<?> future, int timeout, ChronoUnit timeoutUnit, Consumer<RunningTask> onTimeout) {
        this.future = future;
        this.onTimeout = onTimeout;
        deadline = Instant.now().plus(Duration.of(timeout, timeoutUnit));
    }

    public void terminateIfTimedOut() {
        if (Instant.now().isAfter(deadline)) {
            cancel();
            onTimeout.accept(this);
        }
    }

    public synchronized void cancel() { //must be synchronized as it can be canceled from timeoutVerifier thread and monitor thread
        if (!future.isDone()) {
            future.cancel(true);
        }
    }
}
