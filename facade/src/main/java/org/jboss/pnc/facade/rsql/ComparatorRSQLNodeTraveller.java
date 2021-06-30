/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.facade.rsql;

import static org.jboss.pnc.facade.rsql.RSQLProducerImpl.DESC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.LogicalNode;
import java.lang.reflect.Method;
import java.util.Comparator;
import org.jboss.pnc.common.util.StringUtils;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
class ComparatorRSQLNodeTraveller<DTO> extends RSQLNodeTraveller<Comparator<DTO>> {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public Comparator<DTO> visit(LogicalNode logicalNode) {
        return null;
    }

    @Override
    public Comparator<DTO> visit(ComparisonNode node) {
        logger.trace("Sorting direction - {}, arguments {}", node.getOperator(), node.getArguments());
        Comparator<DTO> comparator = null;
        for (String argument : node.getArguments()) {
            Comparator<DTO> comp = Comparator
                    .comparing(dto -> getProperty(dto, argument), Comparator.nullsLast(Comparator.naturalOrder()));
            if (comparator == null) {
                comparator = comp;
            } else {
                comparator = comparator.thenComparing(comp);
            }
        }
        if (comparator == null) {
            throw new RSQLException("No argument for RSQL comparsion found.");
        }
        if (node.getOperator().equals(DESC)) {
            comparator = comparator.reversed();
        }

        return comparator;
    }

    private Comparable getProperty(Object object, String argument) {
        try {
            String getter = "get" + StringUtils.firstCharToUpperCase(argument);
            Method method = object.getClass().getMethod(getter);
            Class<?> returnType = method.getReturnType();
            Object obj = method.invoke(object);
            if (Comparable.class.isAssignableFrom(returnType)) {
                return (Comparable) obj;
            } else {
                throw new RSQLException("Field " + argument + " is not comparable.");
            }
        } catch (NoSuchMethodException ex) {
            throw new RSQLException("Field " + argument + " not found.", ex);
        } catch (ReflectiveOperationException | SecurityException ex) {
            throw new RuntimeException("Could not access field " + argument + ": " + ex.getMessage(), ex);
        }
    }

}
