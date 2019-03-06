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
package org.jboss.pnc.facade.rsql;

import org.jboss.pnc.datastore.predicates.rsql.EmptyRSQLPredicate;
import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;

import java.lang.invoke.MethodHandles;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.RSQLParserException;
import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import cz.jirutka.rsql.parser.ast.Node;
import cz.jirutka.rsql.parser.ast.RSQLOperators;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@ApplicationScoped
public class RSQLPredicateProducerImpl implements RSQLPredicateProducer {

    private static final Logger logger = LoggerFactory.getLogger(RSQLPredicateProducerImpl.class);

    private final RSQLParser parser;

    private final static Pattern likePattern = Pattern.compile("(\\%[a-zA-Z0-9\\s]+\\%)");
    final static String UNKNOWN_PART_PLACEHOLDER = "_";

    final static ComparisonOperator LIKE = new ComparisonOperator("=like=");
    final static ComparisonOperator IS_NULL = new ComparisonOperator("=isnull=");

    public RSQLPredicateProducerImpl() throws RSQLParserException {
        Set<ComparisonOperator> operators = RSQLOperators.defaultOperators();
        operators.add(LIKE);
        operators.add(IS_NULL);

        parser = new RSQLParser(operators);
    }

    @Override
    public <T extends GenericEntity<Integer>> Predicate<T> getCriteriaPredicate(BiFunction<From<?, T>, RSQLSelectorPath, Path> toPath, String rsql) {
        if (rsql == null || rsql.isEmpty()) {
            return new EmptyRSQLPredicate();
        }
        Node rootNode = parser.parse(preprocessRSQL(rsql));
        return getEntityPredicate(rootNode, toPath);
    }

    @Override
    public <T> java.util.function.Predicate<T> getStreamPredicate(String rsql) {
        if (rsql == null || rsql.isEmpty()) {
            return x -> true;
        }
        Node rootNode = parser.parse(preprocessRSQL(rsql));
        return getStreamPredicate(rootNode);
    }

    private String preprocessRSQL(String rsql) {
        String result = rsql;
        Matcher matcher = likePattern.matcher(rsql);
        while (matcher.find()) {
            result = rsql.replaceAll(matcher.group(1), matcher.group(1).replaceAll("\\s", UNKNOWN_PART_PLACEHOLDER));
        }
        return result;
    }

    private <T extends GenericEntity<Integer>> Predicate<T> getEntityPredicate(Node rootNode, BiFunction<From<?, T>, RSQLSelectorPath, Path> toPath) {
        return (root, query, cb) -> {
            RSQLNodeTraveller<javax.persistence.criteria.Predicate> visitor = new EntityRSQLNodeTraveller(root, cb, toPath);

            return rootNode.accept(visitor);
        };
    }

    private <T> java.util.function.Predicate<T> getStreamPredicate(Node rootNode) {
        return instance -> {
            RSQLNodeTraveller<Boolean> visitor = new StreamRSQLNodeTraveller(instance);

            return rootNode.accept(visitor);
        };
    }

}
