/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mock.dto;

import org.jboss.pnc.dto.ProjectRef;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class ProjectMock {

    public static ProjectRef newProjectRef() {
        return ProjectRef.refBuilder()
                .id("1")
                .name("A")
                .description("desc")
                .projectUrl("url1")
                .issueTrackerUrl("url2")
                .build();
    }

}
