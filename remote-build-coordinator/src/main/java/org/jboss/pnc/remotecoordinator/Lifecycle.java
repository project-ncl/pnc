/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.remotecoordinator;

import javax.enterprise.context.Dependent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-16.
 */
@Dependent
public class Lifecycle {

    private static final Logger log = LoggerFactory.getLogger(Lifecycle.class);

    public Lifecycle() {
    }

    public void start() {
        log.info("Core started.");
    }

    public void stop() {
        log.info("Core stopped.");
    }

}
