/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.deliverables;

import java.util.List;

import org.jboss.pnc.api.enums.OperationResult;
import org.jboss.pnc.api.enums.ProgressStatus;

/**
 * @author jakubvanko
 */
public interface DeliverableAnalysisStatusChangedEvent {

    String getOperationId();

    ProgressStatus getStatus();

    OperationResult getResult();

    String getMilestoneId();

    List<String> getDeliverablesUrls();
}
