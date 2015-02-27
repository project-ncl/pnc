package org.jboss.pnc.datastore.limits.rsql;

import cz.jirutka.rsql.parser.UnknownOperatorException;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.RSQLNodesFactory;

import java.util.List;

public class SortingRSQLNodesFactory extends RSQLNodesFactory {

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
