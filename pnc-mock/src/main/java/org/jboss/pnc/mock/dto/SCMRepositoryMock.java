/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mock.dto;

import org.jboss.pnc.dto.SCMRepository;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class SCMRepositoryMock {

    public static SCMRepository newScmRepository() {
        return SCMRepository.builder()
                .id("1")
                .internalUrl("url1")
                .externalUrl("url2")
                .preBuildSyncEnabled(true)
                .build();
    }
}
