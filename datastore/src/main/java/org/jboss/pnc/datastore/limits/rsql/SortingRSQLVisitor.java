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

import cz.jirutka.rsql.parser.ast.AndNode;
import cz.jirutka.rsql.parser.ast.EqualNode;
import cz.jirutka.rsql.parser.ast.GreaterThanNode;
import cz.jirutka.rsql.parser.ast.GreaterThanOrEqualNode;
import cz.jirutka.rsql.parser.ast.InNode;
import cz.jirutka.rsql.parser.ast.LessThanNode;
import cz.jirutka.rsql.parser.ast.LessThanOrEqualNode;
import cz.jirutka.rsql.parser.ast.NotEqualNode;
import cz.jirutka.rsql.parser.ast.NotInNode;
import cz.jirutka.rsql.parser.ast.OrNode;
import cz.jirutka.rsql.parser.ast.RSQLVisitor;

interface SortingRSQLVisitor<R, A> extends RSQLVisitor<R, A> {

    R visit(AscendingSortingNode node, A param);

    R visit(DescendingSortingNode node, A param);

    @Override default R visit(OrNode node, A param) {
        return null;
    }

    @Override default R visit(InNode node, A param) {
        return null;
    }

    @Override default R visit(AndNode node, A param) {
        return null;
    }

    @Override default R visit(EqualNode node, A param) {
        return null;
    }

    @Override default R visit(GreaterThanOrEqualNode node, A param) {
        return null;
    }

    @Override default R visit(GreaterThanNode node, A param) {
        return null;
    }

    @Override default R visit(LessThanOrEqualNode node, A param) {
        return null;
    }

    @Override default R visit(LessThanNode node, A param) {
        return null;
    }

    @Override default R visit(NotEqualNode node, A param) {
        return null;
    }

    @Override default R visit(NotInNode node, A param) {
        return null;
    }
}