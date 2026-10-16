/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.datastore.predicates;

import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.DeliverableAnalyzerOperation_;
import org.jboss.pnc.model.DeliverableAnalyzerReport_;
import org.jboss.pnc.model.DeliverableArtifact;
import org.jboss.pnc.model.DeliverableArtifact_;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;

/**
 * Predicates for {@link org.jboss.pnc.model.DeliverableArtifact} entity.
 */
public class DeliverableArtifactPredicates {

    public static Predicate<DeliverableArtifact> withReportId(Base32LongID reportId) {
        return (root, query, cb) -> cb.equal(
                root.get(DeliverableArtifact_.report)
                        .get(DeliverableAnalyzerReport_.operation)
                        .get(DeliverableAnalyzerOperation_.id),
                reportId);
    }
}
