/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.datastore.repositories;

import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.DeliverableAnalyzerDistribution;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;

/**
 * Interface for manipulating {@link org.jboss.pnc.model.DeliverableAnalyzerDistribution} entity
 */
public interface DeliverableAnalyzerDistributionRepository
        extends Repository<DeliverableAnalyzerDistribution, Base32LongID> {

    DeliverableAnalyzerDistribution queryByUrl(String url);

    DeliverableAnalyzerDistribution queryByUrlAndSha256(String url, String sha256);

}
