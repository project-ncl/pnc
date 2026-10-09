/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mock.repository;

import org.jboss.pnc.model.DeliverableAnalyzerDistribution;
import org.jboss.pnc.spi.datastore.repositories.DeliverableAnalyzerDistributionRepository;

public class DeliverableAnalyzerDistributionRepositoryMock
        extends Base32LongIdRepositoryMock<DeliverableAnalyzerDistribution>
        implements DeliverableAnalyzerDistributionRepository {

    @Override
    public DeliverableAnalyzerDistribution queryByUrl(String url) {
        return data.stream().filter(d -> d.getDistributionUrl().equals(url)).findAny().orElse(null);
    }

    @Override
    public DeliverableAnalyzerDistribution queryByUrlAndSha256(String url, String sha256) {
        return data.stream().filter(d -> {
            return d.getDistributionUrl().equals(url) && d.getSha256().equals(sha256);
        }).findAny().orElse(null);
    }

}
