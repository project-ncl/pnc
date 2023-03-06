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
package org.jboss.pnc.remotecoordinator.builder;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

/**
 * Helper producer of ManagedExecutor to be able to @Inject ManagedScheduledExecutorService in constructor
 *
 * @see https://stackoverflow.com/questions/44295854/cdi-constructor-based-injection-with-resource
 */
@ApplicationScoped
public class ManagedExecutorProducer {

    @Resource
    ManagedScheduledExecutorService scheduledExecutorService;

    @Produces
    public ManagedScheduledExecutorService produceService() {
        return scheduledExecutorService;
    }
}
