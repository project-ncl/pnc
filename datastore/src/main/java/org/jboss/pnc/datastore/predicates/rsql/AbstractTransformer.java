package org.jboss.pnc.datastore.predicates.rsql;

import org.jboss.pnc.model.GenericEntity;

import javax.persistence.criteria.*;
import java.util.List;

abstract class AbstractTransformer<Entity extends GenericEntity<? extends Number>> implements Transformer<Entity> {

    private final ArgumentHelper argumentHelper = new ArgumentHelper();

    @Override
    public Predicate transform(Root<Entity> r, CriteriaBuilder cb, Class<?> selectingClass, String operand, List<String> arguments) {
        return transform(r, selectWithOperand(r, operand), cb, operand, argumentHelper.getConvertedType(selectingClass, operand, arguments));
    }

    public Path<Entity> selectWithOperand(Root<Entity> r, String operand) {
        String [] splittedFields = operand.split("\\.");
        Path<Entity> currentPath = r;
        for(int i = 0; i < splittedFields.length - 1; ++i) {
            currentPath = r.join(splittedFields[i]);
        }
        return currentPath.get(splittedFields[splittedFields.length - 1]);
    }

    abstract Predicate transform(Root<Entity> r, Path<Entity> selectedPath, CriteriaBuilder cb, String operand, List<Object> convertedArguments);
}
