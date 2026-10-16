/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.datastore.predicates;

import org.jboss.pnc.model.DeliverableAnalyzerDistribution;
import org.jboss.pnc.model.DeliverableAnalyzerDistribution_;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;

/**
 * Predicates for {@link org.jboss.pnc.model.DeliverableAnalyzerDistribution} entity.
 */
public class DeliverableAnalyzerDistributionPredicates {

    public static Predicate<DeliverableAnalyzerDistribution> withUrl(String url) {
        return (root, query, cb) -> cb.equal(root.get(DeliverableAnalyzerDistribution_.distributionUrl), url);
    }

    public static Predicate<DeliverableAnalyzerDistribution> withUrlAndSha256(String url, String sha256) {

        return (root, query, cb) -> cb.and(
                cb.equal(root.get(DeliverableAnalyzerDistribution_.distributionUrl), url),
                cb.equal(root.get(DeliverableAnalyzerDistribution_.sha256), sha256));
    }
}
