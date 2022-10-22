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
package org.jboss.pnc.common.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.pnc.common.util.otel.ContextCopier;

/**
 * A thread factory that names threads.
 * <p>
 *
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 8/18/16 Time: 8:18 AM
 */
public class NamedThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolNumber = new AtomicInteger(0);
    private final AtomicInteger threadNumber = new AtomicInteger(0);
    private final String name;
    private final int pool;

    private final List<ContextCopier> contextCopiers;

    /**
     * @param name name of the thread pool
     */
    public NamedThreadFactory(String name) {
        this.name = name;
        this.pool = poolNumber.getAndIncrement();
        this.contextCopiers = new ArrayList<>();
    }

    /**
     * @param name name of the thread pool
     * @param contextCopiers list of implementations of interface contextCopiers
     */
    public NamedThreadFactory(String name, final Collection<ContextCopier> contextCopiers) {
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
            contextCopiers.forEach(ContextCopier::apply);
            r.run();
        };
    }
}
