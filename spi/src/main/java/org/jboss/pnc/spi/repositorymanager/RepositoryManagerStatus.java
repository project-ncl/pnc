/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.repositorymanager;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public enum RepositoryManagerStatus {
    SUCCESS(false),

    VALIDATION_ERROR(true);

    private boolean hasFailed;

    RepositoryManagerStatus(boolean hasFailed) {
        this.hasFailed = hasFailed;
    }

    public boolean hasFailed() {
        return hasFailed;
    }
}
