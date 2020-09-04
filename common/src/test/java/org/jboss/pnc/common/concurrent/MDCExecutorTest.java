/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class MDCExecutorTest {
    private static final Logger logger = LoggerFactory.getLogger(MDCExecutorTest.class);

    ExecutorService executorServiceDefault = MDCExecutors.newFixedThreadPool(4);
    ExecutorService executorService = MDCExecutors.newFixedThreadPool(1);

    @Test
    public void shouldPassMDC() throws InterruptedException, ExecutionException {
        logger.info("Running ...");

        AtomicReference<String> context = new AtomicReference<>();
        AtomicReference<String> modifiedContext = new AtomicReference<>();
        AtomicReference<Future> waitToComplete = new AtomicReference<>();
        AtomicReference<Future> modifiedWaitToComplete = new AtomicReference<>();

        Runnable taskNoContext = () -> logger.info("no-context");
        // each new thread in the pool uses calling thread as a parent
        // run first task to have a main thread as a parent thread
        executorService.submit(taskNoContext);

        Runnable taskHasContext = () -> {
            sleep(200);
            logger.info("has-context");
            context.set(MDC.get("ctx"));
        };

        Runnable taskHasModifiedContext = () -> {
            sleep(200);
            logger.info("has-modified-context");
            modifiedContext.set(MDC.get("ctx"));
        };

        Runnable taskSetContext = () -> {
            logger.info("T1 in");
            Map<String, String> map = new HashMap<>();
            map.put("ctx", "firstValue");
            MDC.setContextMap(map);
            Future submit = executorService.submit(taskHasContext);
            waitToComplete.set(submit);

            map.put("ctx", "firstValueModified");
            MDC.setContextMap(map);
            Future submitModified = executorService.submit(taskHasModifiedContext);
            modifiedWaitToComplete.set(submitModified);

            logger.info("T1 out");
        };

        Runnable taskSetUpdateContext = () -> {
            sleep(100);
            logger.info("T2 in");
            Map<String, String> map = new HashMap<>();
            map.put("ctx", "secondValue");
            MDC.setContextMap(map);
            logger.info("T2 out");
        };

        Future<?> submit = executorServiceDefault.submit(taskSetContext);
        Future<?> submit2 = executorServiceDefault.submit(taskSetUpdateContext);
        submit.get();
        submit2.get();
        waitToComplete.get().get();
        modifiedWaitToComplete.get().get();

        executorServiceDefault.shutdown();
        executorService.shutdown();

        Assert.assertEquals("firstValue", context.get());
        Assert.assertEquals("firstValueModified", modifiedContext.get());
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
