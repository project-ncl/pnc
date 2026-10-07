/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mapper;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.pnc.dto.internal.ProcessException;
import org.jboss.pnc.mapper.api.SimpleMapper;

/**
 *
 * @author Jan Michalov &lt;jmichalo@redhat.com&gt;
 */
@ApplicationScoped
public class ProcessExceptionMapper
        implements SimpleMapper<ProcessException, org.jboss.pnc.spi.coordinator.ProcessException> {

    @Override
    public org.jboss.pnc.spi.coordinator.ProcessException toEntity(ProcessException dto) {
        if (dto == null) {
            return null;
        }
        return new org.jboss.pnc.spi.coordinator.ProcessException(dto.getMessage(), dto.getCause());
    }

    @Override
    public ProcessException toDTO(org.jboss.pnc.spi.coordinator.ProcessException entity) {
        if (entity == null) {
            return null;
        }
        return new ProcessException(entity.getMessage(), entity.getCause());
    }
}
