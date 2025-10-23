/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Iterator;
import java.util.regex.Pattern;

import cz.jirutka.rsql.parser.ast.AndNode;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.LogicalNode;
import cz.jirutka.rsql.parser.ast.Node;
import cz.jirutka.rsql.parser.ast.OrNode;
import cz.jirutka.rsql.parser.ast.RSQLOperators;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
class StreamRSQLNodeTraveller extends RSQLNodeTraveller<Boolean> {

    private static final Logger logger = LoggerFactory.getLogger(StreamRSQLNodeTraveller.class);

    private final Object instance;

    public StreamRSQLNodeTraveller(Object instance) {
        this.instance = instance;
    }

    @Override
    public Boolean visit(LogicalNode node) {
        logger.trace("Parsing LogicalNode {}", node);
        Iterator<Node> iterator = node.iterator();
        if (node instanceof AndNode) {
            boolean result = true;
            while (iterator.hasNext()) {
                Node next = iterator.next();
                result &= visit(next);
            }
            return result;
        } else if (node instanceof OrNode) {
            boolean result = false;
            while (iterator.hasNext()) {
                Node next = iterator.next();
                result |= visit(next);
            }
            return result;
        } else {
            throw new UnsupportedOperationException("Logical operation not supported");
        }
    }

    @Override
    public Boolean visit(ComparisonNode node) {
        logger.trace("Parsing ComparisonNode {}", node);
        String fieldName = node.getSelector();
        String argument = node.getArguments().get(0);
        try {
            String propertyValue = BeanUtils.getProperty(instance, fieldName);
            Class<?> propertyType = PropertyUtils.getPropertyType(instance, fieldName);

            if (node.getOperator().equals(RSQLProducerImpl.IS_NULL)) {
                return Boolean.valueOf(propertyValue == null).equals(Boolean.valueOf(argument));
            }
            if (propertyValue == null) {
                // Null values are considered not equal
                return false;
            }
            if (node.getOperator().equals(RSQLOperators.EQUAL)) {
                if (propertyType == Boolean.class || propertyType == boolean.class) {
                    return Boolean.valueOf(propertyValue).equals(Boolean.valueOf(argument));
                }

                return propertyValue.equals(argument);
            } else if (node.getOperator().equals(RSQLOperators.NOT_EQUAL)) {
                if (propertyType == Boolean.class || propertyType == boolean.class) {
                    return !Boolean.valueOf(propertyValue).equals(Boolean.valueOf(argument));
                }

                return !propertyValue.equals(argument);
            } else if (node.getOperator().equals(RSQLOperators.GREATER_THAN)) {
                NumberFormat numberFormat = NumberFormat.getInstance();
                Number argumentNumber = numberFormat.parse(argument);
                return numberFormat.parse(propertyValue).intValue() > argumentNumber.intValue();
            } else if (node.getOperator().equals(RSQLOperators.GREATER_THAN_OR_EQUAL)) {
                NumberFormat numberFormat = NumberFormat.getInstance();
                Number argumentNumber = numberFormat.parse(argument);
                return numberFormat.parse(propertyValue).intValue() >= argumentNumber.intValue();
            } else if (node.getOperator().equals(RSQLOperators.LESS_THAN)) {
                NumberFormat numberFormat = NumberFormat.getInstance();
                Number argumentNumber = numberFormat.parse(argument);
                return numberFormat.parse(propertyValue).intValue() < argumentNumber.intValue();
            } else if (node.getOperator().equals(RSQLOperators.LESS_THAN_OR_EQUAL)) {
                NumberFormat numberFormat = NumberFormat.getInstance();
                Number argumentNumber = numberFormat.parse(argument);
                return numberFormat.parse(propertyValue).intValue() <= argumentNumber.intValue();
            } else if (node.getOperator().equals(RSQLProducerImpl.LIKE)) {
                return propertyValue.matches(preprocessLikeOperatorArgument(argument));
            } else if (node.getOperator().equals(RSQLProducerImpl.NOT_LIKE)) {
                return !propertyValue.matches(preprocessLikeOperatorArgument(argument));
            } else if (node.getOperator().equals(RSQLOperators.IN)) {
                return node.getArguments().contains(propertyValue);
            } else if (node.getOperator().equals(RSQLOperators.NOT_IN)) {
                return !node.getArguments().contains(propertyValue);
            } else {
                throw new UnsupportedOperationException("Not Implemented yet!");
            }
        } catch (NestedNullException e) {
            // If a nested property is null (i.e. idRev.id is null), it is considered a false equality
            return false;
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(
                    "RSQL selector " + fieldName + " not applicable on the type " + instance.getClass());
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new IllegalStateException("Reflections exception", e);
        } catch (ParseException e) {
            throw new IllegalStateException("RSQL parse exception", e);
        }
    }

    private String preprocessLikeOperatorArgument(String argument) {
        return argument.replaceAll(Pattern.quote(RSQLProducerImpl.WILDCARD_MULTIPLE_CHARACTERS_DB), ".*")
                .replaceAll(Pattern.quote(RSQLProducerImpl.WILDCARD_MULTIPLE_CHARACTERS), ".*")
                .replaceAll(Pattern.quote(RSQLProducerImpl.WILDCARD_SINGLE_CHARACTER), ".");
    }
}
