/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.datastore.predicates.rsql;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Empty implementation of a RSQL adapter
 *
 * <p>
 * Converts RSQL query into Spring Data's {@link org.springframework.data.jpa.domain.Specification}, which in turn might
 * be used for selecting records.
 * </p>
 */
public class EmptyRSQLPredicate implements org.jboss.pnc.spi.datastore.repositories.api.Predicate {

    @Override
    public Predicate apply(Root root, CriteriaQuery query, CriteriaBuilder cb) {
        return cb.conjunction();
    }

}
