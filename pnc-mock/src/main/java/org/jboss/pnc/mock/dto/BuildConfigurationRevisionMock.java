/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mock.dto;

import org.jboss.pnc.dto.BuildConfigurationRevisionRef;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildConfigurationRevisionMock {

    public static BuildConfigurationRevisionRef newBuildConfigurationRevisionRef() {
        return newBuildConfigurationRevisionRef("name");
    }

    public static BuildConfigurationRevisionRef newBuildConfigurationRevisionRef(String name) {
        return BuildConfigurationRevisionRef.refBuilder()
                .id("1")
                .rev(1)
                .name(name)
                .buildScript("true")
                .scmRevision("awqs21")
                .build();
    }

}
