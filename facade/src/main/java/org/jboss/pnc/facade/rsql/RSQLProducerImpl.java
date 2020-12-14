/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.RSQLParserException;
import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import cz.jirutka.rsql.parser.ast.Node;
import cz.jirutka.rsql.parser.ast.RSQLOperators;
import org.jboss.pnc.datastore.limits.rsql.EmptySortInfo;
import org.jboss.pnc.datastore.predicates.rsql.EmptyRSQLPredicate;
import org.jboss.pnc.facade.rsql.mapper.UniversalRSQLMapper;
import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@ApplicationScoped
public class RSQLProducerImpl implements RSQLProducer {

    private static final Logger logger = LoggerFactory.getLogger(RSQLProducerImpl.class);

    private final RSQLParser predicateParser;
    private final RSQLParser sortParser;

    private final static Pattern likePattern = Pattern.compile("(\\%[a-zA-Z0-9\\s]+\\%)");
    final static String UNKNOWN_PART_PLACEHOLDER = "_";
    private static final String FIXED_START_OF_SORTING_EXPRESSION = "sort";

    final static ComparisonOperator LIKE = new ComparisonOperator("=like=", "=LIKE=");
    final static ComparisonOperator NOT_LIKE = new ComparisonOperator("=notlike=", "=NOTLIKE=");
    final static ComparisonOperator IS_NULL = new ComparisonOperator("=isnull=", "=ISNULL=");

    static final ComparisonOperator ASC = new ComparisonOperator("=asc=", true);
    static final ComparisonOperator DESC = new ComparisonOperator("=desc=", true);

    @Inject
    UniversalRSQLMapper mapper;

    public RSQLProducerImpl() {
        Set<ComparisonOperator> predicateOperators = RSQLOperators.defaultOperators();
        predicateOperators.add(LIKE);
        predicateOperators.add(NOT_LIKE);
        predicateOperators.add(IS_NULL);

        predicateParser = new RSQLParser(predicateOperators);

        Set<ComparisonOperator> sortOperators = new HashSet<>();
        sortOperators.add(ASC);
        sortOperators.add(DESC);

        sortParser = new RSQLParser(sortOperators);
    }

    @Override
    public <DB extends GenericEntity<?>> Predicate<DB> getCriteriaPredicate(Class<DB> type, String rsql) {
        if (rsql == null || rsql.isEmpty()) {
            return new EmptyRSQLPredicate();
        }
        try {
            Node rootNode = predicateParser.parse(preprocessRSQL(rsql));
            return getEntityPredicate(rootNode, type);
        } catch (RSQLParserException ex) {
            throw new RSQLException("failure parsing RSQL", ex);
        }
    }

    @Override
    public <T> java.util.function.Predicate<T> getStreamPredicate(String rsql) {
        if (rsql == null || rsql.isEmpty()) {
            return x -> true;
        }
        try {
            Node rootNode = predicateParser.parse(preprocessRSQL(rsql));
            return getStreamPredicate(rootNode);
        } catch (RSQLParserException ex) {
            throw new RSQLException("failure parsing RSQL", ex);
        }
    }

    @Override
    public <DB extends GenericEntity<?>> SortInfo getSortInfo(Class<DB> type, String rsql) {
        if (rsql == null || rsql.isEmpty()) {
            return new EmptySortInfo();
        }

        if (!rsql.startsWith(FIXED_START_OF_SORTING_EXPRESSION)) {
            rsql = FIXED_START_OF_SORTING_EXPRESSION + rsql;
        }

        Node rootNode = sortParser.parse(preprocessRSQL(rsql));
        Function<RSQLSelectorPath, String> toPath = (RSQLSelectorPath selector) -> mapper.toPath(type, selector);
        return (SortInfo) rootNode.accept(new SortRSQLNodeTraveller(toPath));
    }

    @Override
    public <DTO> Comparator<DTO> getComparator(String rsql) {
        if (rsql == null || rsql.isEmpty()) {
            throw new RSQLException("RSQL sort query must be non-empty and non-null.");
        }
        if (!rsql.startsWith(FIXED_START_OF_SORTING_EXPRESSION)) {
            rsql = FIXED_START_OF_SORTING_EXPRESSION + rsql;
        }
        Node rootNode = sortParser.parse(preprocessRSQL(rsql));

        return rootNode.accept(new ComparatorRSQLNodeTraveller<>());
    }

    private String preprocessRSQL(String rsql) {
        String result = rsql;
        Matcher matcher = likePattern.matcher(rsql);
        while (matcher.find()) {
            result = rsql.replaceAll(matcher.group(1), matcher.group(1).replaceAll("\\s", UNKNOWN_PART_PLACEHOLDER));
        }
        return result;
    }

    private <DB extends GenericEntity<?>> Predicate<DB> getEntityPredicate(Node rootNode, Class<DB> type) {
        return (root, query, cb) -> {
            RSQLNodeTraveller<javax.persistence.criteria.Predicate> visitor = new EntityRSQLNodeTraveller(
                    root,
                    cb,
                    (BiFunction<From<?, DB>, RSQLSelectorPath, Path>) (from, selector) -> mapper
                            .toPath(type, from, selector));
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
