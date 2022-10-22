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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class MDCWrappers {

    private static final Logger log = LoggerFactory.getLogger(MDCWrappers.class);

    public static Runnable wrap(final Runnable runnable) {
        final Map<String, String> context = MDC.getCopyOfContextMap();
        log.debug("Wrap -> current context from MDC.getCopyOfContextMap: {}", context);
        return () -> {
            Map previous = MDC.getCopyOfContextMap();
            log.debug("Wrap -> previous context from MDC.getCopyOfContextMap: {}", previous);
            if (context == null) {
                MDC.clear();
                log.debug("  BEFORE RUNNING AFTER MDC.clear(): {}", MDC.getCopyOfContextMap());
            } else {
                MDC.setContextMap(context);
                log.debug("  BEFORE RUNNING AFTER MDC.setContextMap({})", MDC.getCopyOfContextMap());
            }
            try {
                log.debug("Wrap -> runnable.run()");
                runnable.run();
            } finally {
                if (previous == null) {
                    MDC.clear();
                    log.debug("  AFTER RUNNING AFTER MDC.clear(): {}", MDC.getCopyOfContextMap());
                } else {
                    MDC.setContextMap(previous);
                    log.debug("  AFTER RUNNING AFTER MDC.setContextMap({})", MDC.getCopyOfContextMap());
                }
            }
        };
    }

    public static <T> Callable<T> wrap(final Callable<T> callable) {
        final Map<String, String> context = MDC.getCopyOfContextMap();
        log.debug("Wrap -> current context from MDC.getCopyOfContextMap: {}", context);

        return () -> {
            Map previous = MDC.getCopyOfContextMap();
            log.debug("Wrap -> previous context from MDC.getCopyOfContextMap: {}", previous);
            if (context == null) {
                MDC.clear();
                log.debug("  BEFORE RUNNING AFTER MDC.clear(): {}", MDC.getCopyOfContextMap());
            } else {
                MDC.setContextMap(context);
                log.debug("  BEFORE RUNNING AFTER MDC.setContextMap({})", MDC.getCopyOfContextMap());
            }
            try {
                log.debug("Wrap -> callable.run()");
                return callable.call();
            } finally {
                if (previous == null) {
                    MDC.clear();
                    log.debug("  AFTER RUNNING AFTER MDC.clear(): {}", MDC.getCopyOfContextMap());
                } else {
                    MDC.setContextMap(previous);
                    log.debug("  AFTER RUNNING AFTER MDC.setContextMap({})", MDC.getCopyOfContextMap());
                }
            }
        };
    }

    public static <T> Consumer<T> wrap(final Consumer<T> consumer) {
        final Map<String, String> context = MDC.getCopyOfContextMap();
        log.debug("Wrap -> current context from MDC.getCopyOfContextMap: {}", context);

        return (t) -> {
            Map previous = MDC.getCopyOfContextMap();
            log.debug("Wrap -> previous context from MDC.getCopyOfContextMap: {}", previous);
            if (context == null) {
                MDC.clear();
                log.debug("  BEFORE RUNNING AFTER MDC.clear(): {}", MDC.getCopyOfContextMap());
            } else {
                MDC.setContextMap(context);
                log.debug("  BEFORE RUNNING AFTER MDC.setContextMap({})", MDC.getCopyOfContextMap());
            }
            try {
                log.debug("Wrap -> consumer.accept(t)");
                consumer.accept(t);
            } finally {
                if (previous == null) {
                    MDC.clear();
                    log.debug("  AFTER RUNNING AFTER MDC.clear(): {}", MDC.getCopyOfContextMap());
                } else {
                    MDC.setContextMap(previous);
                    log.debug("  AFTER RUNNING AFTER MDC.setContextMap({})", MDC.getCopyOfContextMap());
                }
            }
        };
    }

    public static <T> Collection<Callable<T>> wrapCollection(Collection<? extends Callable<T>> tasks) {
        Collection<Callable<T>> wrapped = new ArrayList<>();
        for (Callable<T> task : tasks) {
            wrapped.add(wrap(task));
        }
        return wrapped;
    }
}
