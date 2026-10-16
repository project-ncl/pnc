/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.client;

import java.util.Optional;

import org.jboss.pnc.dto.requests.BuildPushParameters;
import org.junit.Test;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class QueryAndSortTest {

    @Test
    public void testShouldCompile() throws Exception {
        if (true)
            return; // test never run, only to make sure the client classes are properly generated

        Configuration configuration = Configuration.builder().build();
        ProjectClient projectClient = new ProjectClient(configuration);
        projectClient.getAll();
        projectClient.getAll(Optional.of("asc=id"), Optional.empty());

        BuildClient buildClient = new BuildClient(configuration);
        BuildPushParameters pushRequest = BuildPushParameters.builder().build();
        buildClient.push("", pushRequest);
    }
}
