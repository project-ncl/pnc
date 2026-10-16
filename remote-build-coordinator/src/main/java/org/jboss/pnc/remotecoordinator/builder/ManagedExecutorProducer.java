/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
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
