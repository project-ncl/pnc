/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.datastore;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.spi.coordinator.BuildTaskRef;
import org.jboss.pnc.spi.exception.MissingDataException;
import org.jboss.pnc.spi.exception.RemoteRequestException;

public interface BuildTaskRepository {

    Optional<BuildTaskRef> getSpecific(String taskId) throws RemoteRequestException, MissingDataException;

    List<BuildTaskRef> getBuildTasksByBCSRId(Base32LongID buildConfigSetRecordId)
            throws RemoteRequestException, MissingDataException;

    /**
     * @deprecated Used for tests only
     */
    @Deprecated // used in tests only
    Collection<? extends BuildTaskRef> getAll() throws MissingDataException;

    Collection<BuildTaskRef> getUnfinishedTasks() throws RemoteRequestException, MissingDataException;

    /**
     * @deprecated Used for tests only
     */
    @Deprecated
    boolean isEmpty();

    /**
     * @deprecated if needed debug info should be provided by Rex
     */
    @Deprecated
    String getDebugInfo();

}
