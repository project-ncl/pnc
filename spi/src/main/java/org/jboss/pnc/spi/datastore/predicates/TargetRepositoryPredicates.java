/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.datastore.predicates;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.criteria.Path;

import org.jboss.pnc.model.TargetRepository;
import org.jboss.pnc.model.TargetRepository_;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class TargetRepositoryPredicates {

    public static Predicate<TargetRepository> byIdentifierAndPath(String identifier, String repositoryPath) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get(TargetRepository_.identifier), identifier),
                cb.equal(root.get(TargetRepository_.repositoryPath), repositoryPath));
    }

    public static Predicate<TargetRepository> withIdentifierAndPathIn(
            Set<TargetRepository.IdentifierPath> identifierAndPaths) {
        return (root, query, cb) -> {
            Path<String> identifier = root.get(TargetRepository_.identifier);
            Path<String> path = root.get(TargetRepository_.repositoryPath);
            List<javax.persistence.criteria.Predicate> ands = identifierAndPaths.stream()
                    .map(ip -> cb.and(cb.equal(identifier, ip.getIdentifier()), cb.equal(path, ip.getRepositoryPath())))
                    .collect(Collectors.toList());

            return cb.or(ands.toArray(new javax.persistence.criteria.Predicate[ands.size()]));
        };
    }
}
