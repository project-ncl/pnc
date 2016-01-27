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
package org.jboss.pnc.datastore.limits.rsql;

import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.RSQLVisitor;

import java.util.List;

class AscendingSortingNode extends ComparisonNode {

    public static final String OPERATOR = "=asc=";

    public AscendingSortingNode(String selector, List<String> arguments) {
        super(selector, arguments);
    }

    public String getOperator() {
        return OPERATOR;
    }

    public <R, A> R accept(SortingRSQLVisitor<R, A> visitor, A param) {
        return visitor.visit(this, param);
    }

    @Override
    public <R, A> R accept(RSQLVisitor<R, A> visitor, A param) {
        if(visitor instanceof SortingRSQLVisitor) {
            return accept((SortingRSQLVisitor<R, A>) visitor, param);
        }
        return null;
    }
}
