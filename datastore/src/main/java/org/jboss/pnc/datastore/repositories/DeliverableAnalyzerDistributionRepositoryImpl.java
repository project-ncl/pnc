/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.datastore.repositories;

import javax.ejb.Stateless;

import org.jboss.pnc.common.concurrent.Sequence;
import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.DeliverableAnalyzerDistribution;
import org.jboss.pnc.spi.datastore.predicates.DeliverableAnalyzerDistributionPredicates;
import org.jboss.pnc.spi.datastore.repositories.DeliverableAnalyzerDistributionRepository;

@Stateless
public class DeliverableAnalyzerDistributionRepositoryImpl
        extends AbstractRepository<DeliverableAnalyzerDistribution, Base32LongID>
        implements DeliverableAnalyzerDistributionRepository {

    public DeliverableAnalyzerDistributionRepositoryImpl() {
        super(DeliverableAnalyzerDistribution.class, Base32LongID.class);
    }

    @Override
    public DeliverableAnalyzerDistribution save(DeliverableAnalyzerDistribution entity) {
        if (entity.getId() == null) {
            entity.setId(new Base32LongID(Sequence.nextBase32Id()));
        }
        return super.save(entity);
    }

    @Override
    public DeliverableAnalyzerDistribution queryByUrl(String url) {
        return queryByPredicates(DeliverableAnalyzerDistributionPredicates.withUrl(url));
    }

    @Override
    public DeliverableAnalyzerDistribution queryByUrlAndSha256(String url, String sha256) {
        return queryByPredicates(DeliverableAnalyzerDistributionPredicates.withUrlAndSha256(url, sha256));
    }
}
