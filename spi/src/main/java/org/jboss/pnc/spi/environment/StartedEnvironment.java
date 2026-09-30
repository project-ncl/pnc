/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.environment;

import java.util.function.Consumer;

/**
 * Interface, which represents newly created environment, but the environment is not fully up and running.
 * 
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
public interface StartedEnvironment extends DestroyableEnvironment {

    /**
     * Monitors initialization of environment and notifies consumers after the initialization process. Different
     * consumers are used for successful and unsuccessful initialization result.
     * 
     * @param onComplete Method called after successful environment initialization completed
     * @param onError Method called after unsuccessful initialization
     */
    void monitorInitialization(Consumer<RunningEnvironment> onComplete, Consumer<Exception> onError);

    /**
     * 
     * @return ID of an environment
     */
    String getId();

    void cancel();
}
