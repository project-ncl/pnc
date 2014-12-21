package org.jboss.pnc.core;

import org.jboss.logging.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;


/**
 * Not in use
 *
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-16.
 */
@Deprecated
public class Lifecycle {

    Logger log = Logger.getLogger(Lifecycle.class);

    public Lifecycle(Logger log) {
    }

    public void start() {
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        log.info("Core started.");
    }

    public void stop() {
        log.info("Core stopped.");
    }


}
