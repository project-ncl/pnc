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

import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.DeliverableAnalyzerOperation_;
import org.jboss.pnc.model.DeliverableAnalyzerReport;
import org.jboss.pnc.model.DeliverableAnalyzerReport_;
import org.jboss.pnc.model.DeliverableArtifact;
import org.jboss.pnc.model.DeliverableArtifact_;
import org.jboss.pnc.spi.datastore.repositories.DeliverableAnalyzerReportRepository;

import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Stateless
public class DeliverableAnalyzerReportRepositoryImpl extends AbstractRepository<DeliverableAnalyzerReport, Base32LongID>
        implements DeliverableAnalyzerReportRepository {

    public DeliverableAnalyzerReportRepositoryImpl() {
        super(DeliverableAnalyzerReport.class, Base32LongID.class);
    }

    @Override
    public List<DeliverableArtifact> getAnalyzedArtifacts(Base32LongID id) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DeliverableArtifact> query = cb.createQuery(DeliverableArtifact.class);

        Root<DeliverableArtifact> analyzedArtifacts = query.from(DeliverableArtifact.class);

        query.select(analyzedArtifacts);
        query.where(
                cb.equal(
                        analyzedArtifacts.get(DeliverableArtifact_.report)
                                .get(DeliverableAnalyzerReport_.operation)
                                .get(DeliverableAnalyzerOperation_.id),
                        id));

        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public int countAnalyzedArtifacts(Base32LongID id) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<DeliverableArtifact> analyzedArtifacts = query.from(DeliverableArtifact.class);

        query.select(cb.count(analyzedArtifacts));
        query.where(
                cb.equal(
                        analyzedArtifacts.get(DeliverableArtifact_.report)
                                .get(DeliverableAnalyzerReport_.operation)
                                .get(DeliverableAnalyzerOperation_.id),
                        id));

        return entityManager.createQuery(query).getSingleResult().intValue();
    }
}