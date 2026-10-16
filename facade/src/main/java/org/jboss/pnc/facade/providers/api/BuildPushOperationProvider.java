/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.providers.api;

import org.jboss.pnc.dto.BuildPushOperation;
import org.jboss.pnc.dto.response.Page;

public interface BuildPushOperationProvider
        extends OperationProvider<org.jboss.pnc.model.BuildPushOperation, BuildPushOperation> {

    Page<BuildPushOperation> getOperationsForBuild(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            String buildId);

    Page<BuildPushOperation> getOperationsForMilestone(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            boolean latest,
            String milestoneId);
}
