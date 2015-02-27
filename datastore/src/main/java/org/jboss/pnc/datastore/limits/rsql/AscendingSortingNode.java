package org.jboss.pnc.datastore.limits.rsql;

import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.RSQLVisitor;

import java.util.List;

public class AscendingSortingNode extends ComparisonNode {

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
