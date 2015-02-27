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

public interface SortingRSQLVisitor<R, A> extends RSQLVisitor<R, A> {

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