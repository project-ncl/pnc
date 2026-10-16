/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.providers.api;

import org.jboss.pnc.dto.DeliverableAnalyzerOperation;
import org.jboss.pnc.dto.response.Page;

public interface DeliverableAnalyzerOperationProvider
        extends OperationProvider<org.jboss.pnc.model.DeliverableAnalyzerOperation, DeliverableAnalyzerOperation> {

    Page<DeliverableAnalyzerOperation> getAllDeliverableAnalyzerOperations(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query);

    Page<DeliverableAnalyzerOperation> getAllDeliverableAnalyzerOperationsForMilestone(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            String milestoneId);

}
