/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.repositorymanager.model;

import java.util.function.Consumer;

public interface RunningRepositoryDeletion {

    void monitor(Consumer<CompletedRepositoryDeletion> onComplete, Consumer<Exception> onError);

}
