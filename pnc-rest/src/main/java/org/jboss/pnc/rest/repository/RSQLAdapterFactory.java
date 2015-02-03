package org.jboss.pnc.rest.repository;

/**
 * Entry point for converting RSQL to Criteria.
 */
public class RSQLAdapterFactory {

    /**
     * Converts RSQL to {@link org.springframework.data.jpa.domain.Specification}.
     *
     * @param query RSQL query - may be <code>null</code>.
     * @param <Entity> Entity type.
     * @return New adapter.
     * @throws java.lang.IllegalArgumentException In case of parsing or converting exceptions.
     */
    public static <Entity> RSQLAdapter<Entity> fromRSQL(String query) {
        try {
            if(query == null || query.isEmpty()) {
                return new EmptyRSQLAdapter<>();
            }
            return new RSQLDataStoreSelectorAdapter<>(query);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not parse query: " + query, e);
        }
    }
}
