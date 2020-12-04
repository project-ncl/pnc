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
package org.jboss.pnc.termdbuilddriver.transfer;

import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicInteger;
import static org.assertj.core.api.Assertions.*;

public class DefaultFileTranserTest {

    @Test
    public void testRetryReturnsCorrectValueOnSuccess() throws TransferException, URISyntaxException {

        String returnValue = "Good life";
        DefaultFileTranser transer = new DefaultFileTranser(new URI("http://google.com"), 10);

        AtomicInteger count = new AtomicInteger();

        // we throw some transfer exception at the beginning before 'succeeding'
        DefaultFileTranser.MyRunnable<String> runnable = () -> {

            count.incrementAndGet();
            // throw TransferException for half of the attempts, then succeed
            if (count.get() >= transer.getHttpRetryMaxAttempts() / 2) {
                return returnValue;
            } else {
                throw new TransferException("Failed");
            }
        };

        assertThat(transer.retry(runnable, "test")).isEqualTo(returnValue);
    }

    @Test(expected = TransferException.class)
    public void testRetryExceptionThrownAfterMaxRetries() throws TransferException, URISyntaxException {

        DefaultFileTranser transer = new DefaultFileTranser(new URI("http://google.com"), 10);

        // Make sure a TransferException is thrown after max retries
        transer.retry(() -> {
            throw new TransferException("Failed");
        }, "Make sure transferexception is thrown after retries");
    }

    @Test
    public void testRetriesMaxRetriesAreDoneWithEnoughSleep() throws URISyntaxException {

        DefaultFileTranser transer = new DefaultFileTranser(new URI("http://google.com"), 10);

        // make sure count = max http retries after failures
        AtomicInteger count = new AtomicInteger();

        DefaultFileTranser.MyRunnable<Void> runnable = () -> {
            count.incrementAndGet();
            throw new TransferException("Failed");
        };

        long start = System.currentTimeMillis();

        try {
            transer.retry(runnable, "max retries attempt");
        } catch (TransferException e) {
        }

        long timeElapsed = System.currentTimeMillis() - start;

        assertThat(count.get()).as("We must retry exactly max retry attempts for failures before giving up")
                .isEqualTo(transer.getHttpRetryMaxAttempts());

        // max attempts - 1 because the first attempt has no sleep before.
        assertThat(timeElapsed)
                .as("Time elapsed doing retries must be greater or equal than (max attempts - 1) x sleep time")
                .isGreaterThanOrEqualTo(
                        (transer.getHttpRetryMaxAttempts() - 1) * transer.getHttpRetryWaitBeforeRetry());
    }
}