package org.jboss.pnc.datastore.limits;

import org.jboss.pnc.datastore.limits.rsql.EmptySortInfo;
import org.jboss.pnc.datastore.limits.rsql.RSQLSortInfo;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;
import org.jboss.pnc.spi.datastore.repositories.api.impl.DefaultSortInfo;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DefaultSortInfoProducer implements SortInfoProducer {

    @Override
    public SortInfo getSortInfo(SortInfo.SortingDirection direction, String... fields) {
        return new DefaultSortInfo(direction, fields);
    }

    @Override
    public SortInfo getSortInfo(String rsql) {
        if(rsql == null || rsql.isEmpty()) {
            return new EmptySortInfo();
        }
        return new RSQLSortInfo(rsql);
    }
}
