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

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

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

    /**
     * @param name name of the thread pool
     */
    public NamedThreadFactory(String name) {
        this.name = name;
        this.pool = poolNumber.getAndIncrement();
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, "[" + pool + "]" + name + "-" + threadNumber.getAndIncrement());
    }
}
