package org.jboss.pnc.rest.configuration;

import org.jboss.pnc.core.Lifecycle;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-16.
 */
@Singleton
@Startup
public class LifecycleListener {

    @Inject
    Lifecycle coreLifecycle;

    @PostConstruct
    void atStartup() {
        coreLifecycle.start();
    }

    @PreDestroy
    void atShutdown() {
        coreLifecycle.stop();
    }

}
