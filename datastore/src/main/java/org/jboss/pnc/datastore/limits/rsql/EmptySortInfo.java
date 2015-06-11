package org.jboss.pnc.datastore.limits.rsql;

import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;

import java.util.Arrays;
import java.util.List;

public class EmptySortInfo implements SortInfo {

    @Override
    public List<String> getFields() {
        return Arrays.asList("id");
    }

    @Override
    public SortingDirection getDirection() {
        return SortingDirection.ASC;
    }
}
