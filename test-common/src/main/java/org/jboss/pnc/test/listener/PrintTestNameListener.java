package org.jboss.pnc.test.listener;

import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;

import java.util.logging.Logger;

public class PrintTestNameListener extends RunListener {

    private Logger logger = Logger.getLogger(PrintTestNameListener.class.getName());

    @Override
    public void testStarted(Description description) {
        logger.info("Running test: " + description.getClassName() + "#" + description.getMethodName());
    }
}
