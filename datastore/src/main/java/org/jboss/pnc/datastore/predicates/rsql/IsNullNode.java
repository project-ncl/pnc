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
package org.jboss.pnc.datastore.predicates.rsql;

import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.RSQLVisitor;

import java.util.List;

/**
 * @author Alex Creasy
 */
public class IsNullNode extends ComparisonNode {
    public static final String OPERATOR = "=isnull=";

    public IsNullNode(String selector, List<String> arguments) {
        super(selector, arguments);
    }

    public String getOperator() {
        return OPERATOR;
    }

    @Override
    public <R, A> R accept(RSQLVisitor<R, A> visitor, A param) {
        if (visitor instanceof RSQLNodeTraveller) {
            return ((RSQLNodeTraveller<R>) visitor).visit(this);
        } else {
            throw new IllegalArgumentException("Accepting only RSQLNodeTraveller visitor.");
        }
    }
}