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
package org.jboss.pnc.common.concurrent.otel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TraceAwareNamedThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolNumber = new AtomicInteger(0);
    private final AtomicInteger threadNumber = new AtomicInteger(0);
    private final String name;
    private final int pool;

    private final List<ContextCopier> contextCopiers;

    private final Logger log = LoggerFactory.getLogger(TraceAwareNamedThreadFactory.class);

    /**
     * @param name name of the thread pool
     * @param contextCopiers list of implementations of interface contextCopiers
     */
    public TraceAwareNamedThreadFactory(String name, final Collection<ContextCopier> contextCopiers) {
        log.debug(
                "TraceAwareNamedThreadFactory constructor(String name, final Collection<ContextCopier> contextCopiers)");
        this.name = name;
        this.pool = poolNumber.getAndIncrement();
        this.contextCopiers = new ArrayList<>(contextCopiers);
        this.contextCopiers.forEach(ContextCopier::copy);
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(
                makeRunnableContextCopying(r),
                "[" + pool + "]" + name + "-" + threadNumber.getAndIncrement());
    }

    private Runnable makeRunnableContextCopying(final Runnable r) {

        return () -> {
            log.debug("TraceAwareNamedThreadFactory makeRunnableContextCopying before foreach");
            contextCopiers.forEach(ContextCopier::apply);
            r.run();
            log.debug("TraceAwareNamedThreadFactory makeRunnableContextCopying after foreach");
        };
    }
}
