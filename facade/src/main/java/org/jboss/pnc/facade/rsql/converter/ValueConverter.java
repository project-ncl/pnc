/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.rsql.converter;

import org.jboss.pnc.model.GenericEntity;

public interface ValueConverter {
    <DB extends GenericEntity<?>, T> Comparable<T> convertComparable(Value<DB, T> value);

    <DB extends GenericEntity<?>, T> T convert(Value<DB, T> value);
}
