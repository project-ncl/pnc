package org.jboss.pnc.core;

import org.jboss.logging.Logger;


/**
 * Not in use
 *
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-16.
 */
public class Lifecycle {

    Logger log = Logger.getLogger(Lifecycle.class);

    public Lifecycle() {
    }

    public void start() {
        log.info("Core started.");
    }

    public void stop() {
        log.info("Core stopped.");
    }


}
