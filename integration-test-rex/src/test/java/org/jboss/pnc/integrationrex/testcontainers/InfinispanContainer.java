/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.integrationrex.testcontainers;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public class InfinispanContainer extends GenericContainer<InfinispanContainer> {

    private static final String INFINISPAN_USERNAME = "admin";
    private static final String INFINISPAN_PASSWORD = "password";
    private static final String INFINISPAN_VERSION = "15.0.15.Final";

    public InfinispanContainer(boolean useNative) {
        this(
                "quay.io/infinispan/server"
                        + (useNative ? "-native" + ":" + INFINISPAN_VERSION : ":" + INFINISPAN_VERSION));
    }

    public InfinispanContainer(String imageName) {
        super(imageName);
        addCredentials();
        waitingFor(Wait.forLogMessage(".*ISPN080001.*", 1));
    }

    private void addCredentials() {
        withEnv("USER", INFINISPAN_USERNAME);
        withEnv("PASS", INFINISPAN_PASSWORD);
    }
}
