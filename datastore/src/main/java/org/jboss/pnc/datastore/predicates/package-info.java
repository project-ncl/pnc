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