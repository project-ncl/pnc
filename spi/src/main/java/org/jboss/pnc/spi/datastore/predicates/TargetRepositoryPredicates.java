/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.spi.datastore.predicates;

import org.jboss.pnc.model.TargetRepository;
import org.jboss.pnc.model.TargetRepository_;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;

import javax.persistence.criteria.Expression;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class TargetRepositoryPredicates {

    public static Predicate<TargetRepository> byIdentifierAndPath(String identifier, String repositoryPath) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get(TargetRepository_.identifier), identifier),
                cb.equal(root.get(TargetRepository_.repositoryPath), repositoryPath)
                );
    }

    public static Predicate<TargetRepository> withIdentifierAndPathIn(Set<TargetRepository.IdentifierPath> identifierAndPaths) {
        Set<String> identifiers = identifierAndPaths.stream().map(ip -> ip.getIdentifier()).collect(Collectors.toSet());
        Set<String> identifierAndPathStrings = identifierAndPaths.stream().map(ip -> ip.toString()).collect(Collectors.toSet());
        return (root, query, cb) -> {
            Expression<String> concatPart = cb.concat(root.get(TargetRepository_.identifier), TargetRepository.IdentifierPath.TO_STRING_DELIMITER);
            Expression<String> concat = cb.concat(concatPart, root.get(TargetRepository_.repositoryPath));
            return cb.and(
                    root.get(TargetRepository_.identifier).in(identifiers), //optimization: don't concatenate all the entries
                    concat.in(identifierAndPathStrings)
            );
        };
    }
}
