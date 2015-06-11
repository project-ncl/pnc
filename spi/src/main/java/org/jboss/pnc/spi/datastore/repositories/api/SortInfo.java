package org.jboss.pnc.spi.datastore.repositories.api;

import java.util.List;

public interface SortInfo {
    enum SortingDirection {
        ASC, DESC
    }
    List<String> getFields();
    SortingDirection getDirection();
}
