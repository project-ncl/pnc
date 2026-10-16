/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.requests.labels;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * Generic label request used for POST and DELETE operations above labels (which is some enum).
 */
@Getter
@SuperBuilder(toBuilder = true)
public abstract class GenericLabelRequest<E extends Enum<E>> {

    /**
     * The label being added (removed).
     */
    private E label;

    /**
     * The reason why is this label being added (removed).
     */
    private String reason;
}
