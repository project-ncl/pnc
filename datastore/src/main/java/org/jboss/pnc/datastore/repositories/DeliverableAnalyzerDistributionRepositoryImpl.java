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
import org.jboss.pnc.model.DeliverableAnalyzerDistribution;
import org.jboss.pnc.spi.datastore.predicates.DeliverableAnalyzerDistributionPredicates;
import org.jboss.pnc.spi.datastore.repositories.DeliverableAnalyzerDistributionRepository;

import javax.ejb.Stateless;

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
