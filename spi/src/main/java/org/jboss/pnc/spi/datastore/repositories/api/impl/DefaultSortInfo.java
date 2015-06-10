package org.jboss.pnc.spi.datastore.repositories.api.impl;

import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class DefaultSortInfo implements SortInfo {

    public static final SortingDirection DEFAULT_SORT_DIRECTION = SortingDirection.ASC;

    protected final List<String> fields = new ArrayList<>();
    protected final SortingDirection direction;

    public DefaultSortInfo(SortingDirection direction, String... fields) {
        Collections.addAll(this.fields, fields);
        this.direction = direction;
    }

    public DefaultSortInfo(SortingDirection direction, Collection<String> fields) {
        this.fields.addAll(fields);
        this.direction = direction;
    }

    public DefaultSortInfo() {
        this.direction = DEFAULT_SORT_DIRECTION;
        fields.add("id");
    }

    @Override
    public List<String> getFields() {
        return Collections.unmodifiableList(fields);
    }

    @Override
    public SortingDirection getDirection() {
        return direction;
    }
}
