/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mapper.api;

import org.jboss.pnc.dto.internal.RepourResultRest;
import org.jboss.pnc.spi.repour.RepourResult;
import org.mapstruct.Mapper;

/**
 *
 * @author Jan Michalov &lt;jmichalo@redhat.com&gt;
 */
@Mapper(config = MapperCentralConfig.class)
public interface RepourResultMapper extends SimpleMapper<RepourResultRest, RepourResult> {

    @Override
    RepourResultRest toDTO(RepourResult entity);

    @Override
    RepourResult toEntity(RepourResultRest repourResultRest);
}
