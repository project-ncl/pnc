/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.datastore.predicates;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.jboss.pnc.api.enums.DeliverableAnalyzerReportLabel;
import org.jboss.pnc.model.DeliverableAnalyzerReport;
import org.jboss.pnc.model.DeliverableAnalyzerReport_;

/**
 * Predicates for {@link org.jboss.pnc.model.DeliverableAnalyzerReport} entity.
 */
public class DeliverableAnalyzerReportPredicates {

    public static Predicate notFromScratchAnalysis(
            CriteriaBuilder cb,
            Path<DeliverableAnalyzerReport> deliverableAnalyzerReports) {
        return getNotFromReportLabelAnalysisPredicate(
                cb,
                deliverableAnalyzerReports,
                DeliverableAnalyzerReportLabel.SCRATCH);
    }

    public static Predicate notFromDeletedAnalysis(
            CriteriaBuilder cb,
            Path<DeliverableAnalyzerReport> deliverableAnalyzerReports) {
        return getNotFromReportLabelAnalysisPredicate(
                cb,
                deliverableAnalyzerReports,
                DeliverableAnalyzerReportLabel.DELETED);
    }

    private static Predicate getNotFromReportLabelAnalysisPredicate(
            CriteriaBuilder cb,
            Path<DeliverableAnalyzerReport> deliverableAnalyzerReports,
            DeliverableAnalyzerReportLabel reportLabel) {
        Expression<String> reportLabels = deliverableAnalyzerReports.get(DeliverableAnalyzerReport_.labels)
                .as(String.class);
        return cb.notLike(reportLabels, "%" + reportLabel.name() + "%");
    }
}
