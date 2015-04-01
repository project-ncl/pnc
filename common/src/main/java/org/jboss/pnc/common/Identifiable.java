package org.jboss.pnc.common;

import java.io.Serializable;

/**
 * Interface to allow generic checks on model entities and
 * other classes with an "id" field.
 *
 * @param <IdType> The type of the id field
 */
public interface Identifiable<IdType extends Serializable> {

    public IdType getId();
}
