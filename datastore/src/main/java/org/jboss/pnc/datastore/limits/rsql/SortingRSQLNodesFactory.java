/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.datastore.limits.rsql;

import cz.jirutka.rsql.parser.UnknownOperatorException;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.RSQLNodesFactory;

import java.util.List;

class SortingRSQLNodesFactory extends RSQLNodesFactory {

    @Override
    public ComparisonNode createComparisonNode(String operator, String selector, List<String> arguments) throws UnknownOperatorException { switch (operator) {
            case AscendingSortingNode.OPERATOR:
                return new AscendingSortingNode(selector, arguments);
            case DescendingSortingNode.OPERATOR:
                return new DescendingSortingNode(selector, arguments);
            default:
                return super.createComparisonNode(operator, selector, arguments);
        }
    }
}
