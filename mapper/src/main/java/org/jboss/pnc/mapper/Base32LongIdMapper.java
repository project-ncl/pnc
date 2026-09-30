/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mapper;

import org.jboss.pnc.mapper.api.IdMapper;
import org.jboss.pnc.model.Base32LongID;

public class Base32LongIdMapper implements IdMapper<Base32LongID, String> {

    @Override
    public Base32LongID toEntity(String id) {
        return new Base32LongID(id);
    }

    @Override
    public String toDto(Base32LongID id) {
        return id.getId();
    }
}
