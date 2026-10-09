/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.datastore.repositories;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.jboss.pnc.common.concurrent.Sequence;
import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.DeliverableAnalyzerLabelEntry;
import org.jboss.pnc.model.DeliverableAnalyzerLabelEntry_;
import org.jboss.pnc.model.DeliverableAnalyzerReport_;
import org.jboss.pnc.spi.datastore.repositories.DeliverableAnalyzerLabelEntryRepository;

@Stateless
public class DeliverableAnalyzerLabelEntryRepositoryImpl
        extends AbstractRepository<DeliverableAnalyzerLabelEntry, Base32LongID>
        implements DeliverableAnalyzerLabelEntryRepository {

    public DeliverableAnalyzerLabelEntryRepositoryImpl() {
        super(DeliverableAnalyzerLabelEntry.class, Base32LongID.class);
    }

    @Override
    public DeliverableAnalyzerLabelEntry save(DeliverableAnalyzerLabelEntry entity) {
        if (entity.getId() == null) {
            entity.setId(new Base32LongID(Sequence.nextBase32Id()));
        }
        return super.save(entity);
    }

    @Override
    public Integer getLatestChangeOrderOfReport(Base32LongID id) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Integer> query = cb.createQuery(Integer.class);

        Root<DeliverableAnalyzerLabelEntry> deliverableAnalyzerReportsLabelHistory = query
                .from(DeliverableAnalyzerLabelEntry.class);

        query.select(cb.max(deliverableAnalyzerReportsLabelHistory.get(DeliverableAnalyzerLabelEntry_.changeOrder)));
        query.where(
                cb.equal(
                        deliverableAnalyzerReportsLabelHistory.get(DeliverableAnalyzerLabelEntry_.report)
                                .get(DeliverableAnalyzerReport_.id),
                        id));

        try {
            Integer singleResult = entityManager.createQuery(query).getSingleResult();
            if (singleResult == null) {
                return 0;
            }
            return singleResult;
        } catch (NoResultException ex) {
            // In case the label history is empty, return the starting orderId
            return 0;
        }
    }
}
