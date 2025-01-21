/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.spi.datastore.predicates;

import org.jboss.pnc.api.enums.DeliverableAnalyzerReportLabel;
import org.jboss.pnc.model.DeliverableAnalyzerReport;
import org.jboss.pnc.model.DeliverableAnalyzerReport_;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

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
        Expression reportLabels = deliverableAnalyzerReports.get(DeliverableAnalyzerReport_.labels).as(String.class);
        return cb.notLike(reportLabels, "%" + reportLabel.name() + "%");
    }
}
