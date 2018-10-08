/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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

import javax.persistence.EmbeddedId;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.List;

abstract class AbstractTransformer<Entity> implements Transformer<Entity> {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ArgumentHelper argumentHelper = new ArgumentHelper();

    @Override
    public Predicate transform(Root<Entity> r, CriteriaBuilder cb, Class<?> selectingClass, String operand, List<String> arguments) {
        return transform(r, selectWithOperand(r, operand, selectingClass), cb, operand,
                argumentHelper.getConvertedType(selectingClass, operand, arguments));
    }

    public static Path<?> selectWithOperand(Root<?> root, String operand, Class clazz) {
        Class<?> currentClass = clazz;
        boolean isFieldEmbedded = false;
        String[] fields = operand.split("\\.");

        Path<?> path = root;
        for (int i = 0; i < fields.length - 1; i++) {

            try {
                // Get the field declaration
                Field field = currentClass.getDeclaredField(fields[i]);
                // Is the field annotated with EmbeddedId class?
                isFieldEmbedded = field.getAnnotation(EmbeddedId.class) != null ? true : false;
                // search the field class recursively
                currentClass = field.getType();
            } catch (NoSuchFieldException e) {
                throw new RSQLConverterException("Unable to get class for field " + fields[i], e);
            }

            if (isFieldEmbedded) {
                logger.trace("field {} is EMBEDDED {}", fields[i], isFieldEmbedded);
            }

            if (i == 0) {
                if (!isFieldEmbedded) {
                    path = ((Root<?>) path).join(fields[i]);
                } else {
                    // do not join as it's embedded
                    path = ((Root<?>) path).get(fields[i]);
                }
            } else {
                if (!isFieldEmbedded) {
                    path = ((Join<?, ?>) path).join(fields[i]);
                } else {
                    // do not join as it's embedded
                    path = ((Join<?, ?>) path).get(fields[i]);
                }
            }
        }

        return path.get(fields[fields.length - 1]);
    }

    abstract Predicate transform(Root<Entity> r, Path<?> selectedPath, CriteriaBuilder cb, String operand, List<Object> convertedArguments);
}
