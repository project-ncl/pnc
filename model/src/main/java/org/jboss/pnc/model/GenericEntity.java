/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.model;

import java.io.Serializable;

/**
 * Generic Entity interface. All entities should implement it.
 */
public interface GenericEntity<ID extends Serializable> extends Serializable {
    ID getId();

    void setId(ID id);
}
