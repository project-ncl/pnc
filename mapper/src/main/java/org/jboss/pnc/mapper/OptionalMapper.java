/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mapper;

import java.util.Optional;

/**
 *
 * @author Jan Michalov &lt;jmichalo@redhat.com&gt;
 */
public class OptionalMapper {

    public static <T> Optional<T> wrap(T entity) {
        return Optional.ofNullable(entity);
    }

    public static <T> T unwrap(Optional<T> optional) {
        return (optional != null && optional.isPresent()) ? optional.get() : null;
    }
}
