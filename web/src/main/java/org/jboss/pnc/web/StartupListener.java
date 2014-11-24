package org.jboss.pnc.web;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.logging.Logger;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
public class StartupListener implements ServletContextListener {

    @Inject
    Logger log;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        log.info("Application PNC started.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        log.info("Application PNC stopped.");
    }
}
