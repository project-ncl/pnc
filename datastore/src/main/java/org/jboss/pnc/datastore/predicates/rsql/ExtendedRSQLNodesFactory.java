/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.datastore.predicates.rsql;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.UnknownOperatorException;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.RSQLNodesFactory;

import java.util.List;

/**
 * Add LIKE operator node to the default nodes by extending default factory
 * and passing it into {@link RSQLParser}
 * in {@link RSQLNodeTravellerPredicate#RSQLNodeTravellerPredicate(Class, String)}.
 */
class ExtendedRSQLNodesFactory extends RSQLNodesFactory {

    @Override
    public ComparisonNode createComparisonNode(String operator, String selector, List<String> arguments) throws UnknownOperatorException {
        if (LikeNode.OPERATOR.equals(operator)) {
            return new LikeNode(selector, arguments);
        } else {
            return super.createComparisonNode(operator, selector, arguments);
        }
    }
}
