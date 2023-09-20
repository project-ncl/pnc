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
package org.jboss.pnc.datastore.repositories;

import org.jboss.pnc.common.concurrent.Sequence;
import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.DeliverableAnalyzerLabelEntry;
import org.jboss.pnc.model.DeliverableAnalyzerLabelEntry_;
import org.jboss.pnc.model.DeliverableAnalyzerReport_;
import org.jboss.pnc.spi.datastore.repositories.DeliverableAnalyzerLabelEntryRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@ApplicationScoped
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
        query.where(cb.equal(deliverableAnalyzerReportsLabelHistory.get(DeliverableAnalyzerLabelEntry_.report).get(DeliverableAnalyzerReport_.id), id));

        try {
            return entityManager.createQuery(query).getSingleResult();
        } catch (NoResultException ex) {
            // In case the label history is empty, return the starting orderId
            return 1;
        }
    }
}
