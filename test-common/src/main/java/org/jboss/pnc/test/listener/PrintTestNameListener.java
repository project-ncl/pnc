package org.jboss.pnc.test.listener;

import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrintTestNameListener extends RunListener {

    private Logger logger = LoggerFactory.getLogger(PrintTestNameListener.class);

    @Override
    public void testStarted(Description description) {
        logger.info("Running test: {}#{}", description.getClassName(), description.getMethodName());
    }
}
