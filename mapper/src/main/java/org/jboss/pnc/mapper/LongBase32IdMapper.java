/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mapper;

import org.jboss.pnc.common.pnc.LongBase32IdConverter;
import org.jboss.pnc.mapper.api.IdMapper;

public class LongBase32IdMapper implements IdMapper<Long, String> {

    @Override
    public Long toEntity(String id) {
        return LongBase32IdConverter.toLong(id);
    }

    @Override
    public String toDto(Long id) {
        return LongBase32IdConverter.toString(id);
    }
}
