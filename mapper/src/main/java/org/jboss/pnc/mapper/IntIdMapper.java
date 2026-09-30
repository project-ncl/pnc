/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mapper;

import org.jboss.pnc.mapper.api.IdMapper;

public class IntIdMapper implements IdMapper<Integer, String> {

    @Override
    public Integer toEntity(String s) {
        return Integer.valueOf(s);
    }

    @Override
    public String toDto(Integer integer) {
        return integer.toString();
    }
}
