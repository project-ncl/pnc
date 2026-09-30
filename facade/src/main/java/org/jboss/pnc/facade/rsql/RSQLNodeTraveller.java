/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.rsql;

import cz.jirutka.rsql.parser.ast.AndNode;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.LogicalNode;
import cz.jirutka.rsql.parser.ast.NoArgRSQLVisitorAdapter;
import cz.jirutka.rsql.parser.ast.Node;
import cz.jirutka.rsql.parser.ast.OrNode;

abstract class RSQLNodeTraveller<T> extends NoArgRSQLVisitorAdapter<T> {

    public abstract T visit(LogicalNode logicalNode);

    @Override
    public abstract T visit(ComparisonNode logicalNode);

    public T visit(Node node) {
        // remember overloading is chosen based on static type.
        if (node instanceof LogicalNode) {
            return visit((LogicalNode) node);
        } else if (node instanceof ComparisonNode) {
            return visit((ComparisonNode) node);
        } else {
            throw new UnsupportedOperationException("Did you invent 3rd type of the node?");
        }
    }

    @Override
    public T visit(AndNode node) {
        return visit((LogicalNode) node);
    }

    @Override
    public T visit(OrNode node) {
        return visit((LogicalNode) node);
    }

}
