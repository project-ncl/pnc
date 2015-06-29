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
