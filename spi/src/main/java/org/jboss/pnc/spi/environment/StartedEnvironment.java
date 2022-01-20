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
