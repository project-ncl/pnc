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
/**
 * Query DSL Predicates for querying Database.
 *
 * <p>
 *     The main advantage of using Predicates instead of writing JPQL queries is type safety. Additionally Predicate queries
 *     might be combined together based on <code>or</code> and <code>and</code> operator.
 * </p>
 *
 * <i>Query DSL is based on Domain Driven Design and a Specification Design Pattern.</i>
 *
 * @see com.mysema.query.types.expr.BooleanExpression
 * @see com.mysema.query.types.Predicate
 * @see <a href="http://en.wikipedia.org/wiki/Specification_pattern">Specification Design Pattern</a>
 * @author Sebastian Laskawiec
 */
package org.jboss.pnc.datastore.predicates;