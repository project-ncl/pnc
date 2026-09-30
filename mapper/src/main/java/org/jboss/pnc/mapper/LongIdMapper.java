/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mapper;

import org.jboss.pnc.mapper.api.IdMapper;

public class LongIdMapper implements IdMapper<Long, String> {

    @Override
    public Long toEntity(String id) {
        return Long.valueOf(id);
    }

    @Override
    public String toDto(Long id) {
        return id.toString();
    }
}
