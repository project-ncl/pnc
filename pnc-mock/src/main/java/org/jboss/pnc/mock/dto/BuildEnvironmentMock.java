/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mock.dto;

import org.jboss.pnc.dto.Environment;
import org.jboss.pnc.enums.SystemImageType;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildEnvironmentMock {

    public static Environment newBuildEnvironment() {
        return Environment.builder()
                .id("1")
                .name("jdk8")
                .description("desc")
                .systemImageRepositoryUrl("url")
                .systemImageId("11")
                .systemImageType(SystemImageType.DOCKER_IMAGE)
                .deprecated(true)
                .build();
    }
}
