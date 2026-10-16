/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mapper.api;

import org.jboss.pnc.dto.internal.SshCredentialsRest;
import org.jboss.pnc.spi.SshCredentials;
import org.mapstruct.Mapper;

/**
 *
 * @author Jan Michalov &lt;jmichalo@redhat.com&gt;
 */
@Mapper(config = MapperCentralConfig.class)
public interface SshCredentialsMapper extends SimpleMapper<SshCredentialsRest, SshCredentials> {

    @Override
    SshCredentials toEntity(SshCredentialsRest sshCredentialsRest);

    @Override
    SshCredentialsRest toDTO(SshCredentials entity);
}
