package org.jboss.pnc.model;

import java.io.Serializable;

/**
 * Generic Entity interface. All entities should implement it.
 */
public interface GenericEntity<ID extends Number> extends Serializable {
    ID getId();
}
