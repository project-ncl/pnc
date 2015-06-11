package org.jboss.pnc.spi.datastore.repositories;

import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;

/**
 * Creates sort info based on provided direction and fields.
 */
public interface SortInfoProducer {

    /**
     * Gets sort info based on direction and fields.
     *
     * @param direction Sorting direction.
     * @param fields Fields used for sorting, e.g. <code>"id"</code>
     * @return Sort Info object.
     */
    SortInfo getSortInfo(SortInfo.SortingDirection direction, String... fields);

    /**
     * Gets sort info based RSQL query.
     *
     * @param RSQL query for sorting, e.g. <code>"=asc=id"</code>.
     * @return Sort Info object.
     */
    SortInfo getSortInfo(String rsql);
}
