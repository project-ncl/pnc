package org.jboss.pnc.datastore.predicates;

import org.jboss.pnc.datastore.predicates.rsql.EmptyRSQLPredicate;
import org.jboss.pnc.datastore.predicates.rsql.RSQLNodeTravellerPredicate;

/**
 * Entry point for converting RSQL to Criteria.
 */
public interface RSQLPredicateProducer {

    /**
     * Converts RSQL to {@link org.springframework.data.jpa.domain.Specification}.
     *
     * @param query RSQL query - may be <code>null</code>.
     * @return New adapter.
     * @throws java.lang.IllegalArgumentException In case of parsing or converting exceptions.
     */
    public static <Entity> RSQLPredicate fromRSQL(Class<Entity> selectingClass, String query) {
        try {
            if(query == null || query.isEmpty()) {
                return new EmptyRSQLPredicate();
            }
            return new RSQLNodeTravellerPredicate<>(selectingClass, query);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not parse query: " + query, e);
        }
    }
}
