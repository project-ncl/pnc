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

import com.google.common.base.Preconditions;
import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.RSQLParserException;
import cz.jirutka.rsql.parser.ast.AndNode;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.EqualNode;
import cz.jirutka.rsql.parser.ast.GreaterThanNode;
import cz.jirutka.rsql.parser.ast.GreaterThanOrEqualNode;
import cz.jirutka.rsql.parser.ast.InNode;
import cz.jirutka.rsql.parser.ast.LessThanNode;
import cz.jirutka.rsql.parser.ast.LessThanOrEqualNode;
import cz.jirutka.rsql.parser.ast.LogicalNode;
import cz.jirutka.rsql.parser.ast.Node;
import cz.jirutka.rsql.parser.ast.NotEqualNode;
import cz.jirutka.rsql.parser.ast.NotInNode;
import cz.jirutka.rsql.parser.ast.OrNode;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.NestedNullException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.jboss.pnc.datastore.predicates.rsql.AbstractTransformer.selectWithOperand;

public class RSQLNodeTravellerPredicate<Entity> {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Node rootNode;

    private final Class<Entity> selectingClass;

    private final Map<Class<? extends ComparisonNode>, Transformer<Entity>> operations = new HashMap<>();

    private final static Pattern likePattern = Pattern.compile("(\\%[a-zA-Z0-9\\s]+\\%)");
    private String UNKNOWN_PART_PLACEHOLDER = "_";

    public RSQLNodeTravellerPredicate(Class<Entity> entityClass, String rsql) throws RSQLParserException {
        operations.put(EqualNode.class, new AbstractTransformer<Entity>() {
            @Override
            Predicate transform(Root<Entity> r, Path<?> selectedPath, CriteriaBuilder cb, String operand, List<Object> convertedArguments) {
                return cb.equal(selectedPath, convertedArguments.get(0));
            }
        });

        operations.put(NotEqualNode.class, new AbstractTransformer<Entity>() {
            @Override
            Predicate transform(Root<Entity> r, Path<?> selectedPath, CriteriaBuilder cb, String operand, List<Object> convertedArguments) {
                return cb.notEqual(selectedPath, convertedArguments.get(0));
            }
        });

        operations.put(GreaterThanNode.class, (r, cb, clazz, operand, arguments) -> cb.greaterThan((Path) selectWithOperand(r, operand, clazz), arguments.get(0)));
        operations.put(GreaterThanOrEqualNode.class, (r, cb, clazz, operand, arguments) -> cb.greaterThanOrEqualTo((Path) selectWithOperand(r, operand, clazz), arguments.get(0)));
        operations.put(LessThanNode.class, (r, cb, clazz, operand, arguments) -> cb.lessThan((Path) selectWithOperand(r, operand, clazz), arguments.get(0)));
        operations.put(LessThanOrEqualNode.class, (r, cb, clazz, operand, arguments) -> cb.lessThanOrEqualTo((Path) selectWithOperand(r, operand, clazz), arguments.get(0)));
        operations.put(InNode.class, (r, cb, clazz, operand, arguments) -> ((Path) selectWithOperand(r, operand, clazz)).in(arguments));
        operations.put(NotInNode.class, (r, cb, clazz, operand, arguments) -> cb.not(((Path) selectWithOperand(r, operand, clazz)).in(arguments)));
        operations.put(LikeNode.class, (r, cb, clazz, operand, arguments) -> cb.like(cb.lower((Path) selectWithOperand(r, operand, clazz)), arguments.get(0).toLowerCase()));
        operations.put(IsNullNode.class, (r, cb, clazz, operand, arguments) -> {
            if (Boolean.parseBoolean(arguments.get(0))) {
                return cb.isNull((Path) selectWithOperand(r, operand, clazz));
            } else {
                return cb.isNotNull((Path) selectWithOperand(r, operand, clazz));
            }
        });

        rootNode = new RSQLParser(new ExtendedRSQLNodesFactory()).parse(preprocessRSQL(rsql));
        selectingClass = entityClass;
    }

    public org.jboss.pnc.spi.datastore.repositories.api.Predicate<Entity> getEntityPredicate() {
        return (root, query, cb) -> {
            RSQLNodeTraveller<Predicate> visitor = new RSQLNodeTraveller<Predicate>() {

                public Predicate visit(LogicalNode node) {
                    logger.trace("Parsing LogicalNode {}", node);
                    return proceedEmbeddedNodes(node);
                }

                public Predicate visit(ComparisonNode node) {
                    logger.trace("Parsing ComparisonNode {}", node);
                    return proceedSelection(node);
                }

                private Predicate proceedSelection(ComparisonNode node) {
                    Transformer<Entity> transformation = operations.get(node.getClass());
                    Preconditions.checkArgument(transformation != null, "Operation not supported");
                    return transformation.transform(root, cb, selectingClass, node.getSelector(), node.getArguments());
                }

                private Predicate proceedEmbeddedNodes(LogicalNode node) {
                    Iterator<Node> iterator = node.iterator();
                    if (node instanceof AndNode) {
                        return cb.and(visit(iterator.next()), visit(iterator.next()));
                    } else if (node instanceof OrNode) {
                        return cb.or(visit(iterator.next()), visit(iterator.next()));
                    } else {
                        throw new UnsupportedOperationException("Logical operation not supported");
                    }
                }
            };

            return rootNode.accept(visitor);
        };
    }

    public java.util.function.Predicate<Entity> getStreamPredicate() {
        return instance -> {
            RSQLNodeTraveller<Boolean> visitor = new RSQLNodeTraveller<Boolean>() {

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

                public Boolean visit(ComparisonNode node) {
                    logger.trace("Parsing ComparisonNode {}", node);

                    String fieldName = node.getSelector();
                    String argument = node.getArguments().get(0);

                    try {
                        String propertyValue = BeanUtils.getProperty(instance, fieldName);


                        if (node.getOperator().equals(IsNullNode.OPERATOR)) {
                            return Boolean.valueOf(propertyValue == null).equals(Boolean.valueOf(argument));
                        }

                        if (propertyValue == null) {
                            // Null values are considered not equal
                            return false;
                        }

                        switch (node.getOperator()) {
                            case "==": {
                                return propertyValue.equals(argument);
                            }
                            case "!=": {
                                return !propertyValue.equals(argument);
                            }
                            case ">":
                            case "=gt=": {
                                NumberFormat numberFormat = NumberFormat.getInstance();
                                Number argumentNumber = numberFormat.parse(argument);
                                return numberFormat.parse(propertyValue).intValue() < argumentNumber.intValue();
                            }

                            case ">=":
                            case "=ge=": {
                                NumberFormat numberFormat = NumberFormat.getInstance();
                                Number argumentNumber = numberFormat.parse(argument);
                                return numberFormat.parse(propertyValue).intValue() <= argumentNumber.intValue();
                            }

                            case "<":
                            case "=lt=": {
                                NumberFormat numberFormat = NumberFormat.getInstance();
                                Number argumentNumber = numberFormat.parse(argument);
                                return numberFormat.parse(propertyValue).intValue() > argumentNumber.intValue();
                            }

                            case "<=":
                            case "=le=": {
                                NumberFormat numberFormat = NumberFormat.getInstance();
                                Number argumentNumber = numberFormat.parse(argument);
                                return numberFormat.parse(propertyValue).intValue() >= argumentNumber.intValue();
                            }

                            case "=like=": {
                                argument = argument.replaceAll(UNKNOWN_PART_PLACEHOLDER, ".*").replaceAll("%", ".*");
                                return propertyValue.matches(argument);
                            }

                            case "=in=": {
                                return node.getArguments().contains(propertyValue);
                            }

                            case "=out=": {
                                return !node.getArguments().contains(propertyValue);
                            }

                            default: {
                                throw new UnsupportedOperationException("Not Implemented yet!");
                            }
                        }
                    } catch (NestedNullException e) {
                        // If a nested property is null (i.e. idRev.id is null), it is considered a false equality
                        return false;
                    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                        throw new IllegalStateException("Reflections exception", e);
                    } catch (ParseException e) {
                        throw new IllegalStateException("RSQL parse exception", e);
                    }
                }
            };

            return rootNode.accept(visitor);
        };
    }

    private String preprocessRSQL(String rsql) {
        String result = rsql;
        Matcher matcher = likePattern.matcher(rsql);
        while (matcher.find()) {
            result = rsql.replaceAll(matcher.group(1), matcher.group(1).replaceAll("\\s", UNKNOWN_PART_PLACEHOLDER));
        }
        return result;
    }

}
