/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.validation.model;

import javax.xml.bind.annotation.XmlType;

import org.jboss.pnc.facade.validation.InvalidEntityException;

@XmlType
public class InvalidEntityDetailsRest {

    private String field;

    public InvalidEntityDetailsRest() {
    }

    public InvalidEntityDetailsRest(InvalidEntityException invalidEntityException) {
        this.field = invalidEntityException.getField();
    }

    public String getField() {
        return field;
    }
}
