package org.jboss.pnc.core;

import org.jboss.pnc.core.builder.BuildConsumer;

import javax.inject.Inject;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-16.
 */
public class Lifecycle {

    private BuildConsumer buildConsumer;
    private Thread buildConsumerThread;

    Logger log;

    @Inject
    public Lifecycle(Logger log, BuildConsumer buildConsumer) {
        this.log = log;
        this.buildConsumer = buildConsumer;
    }

    public void start() {
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        buildConsumerThread = threadFactory.newThread(buildConsumer);
        buildConsumerThread.start();
        log.info("Core started.");
    }

    public void stop() {
        buildConsumerThread.interrupt();
        log.info("Core stopped.");
    }


}
