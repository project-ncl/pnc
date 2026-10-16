/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.remotecoordinator.test.dependencies;

import org.jboss.pnc.api.enums.RebuildMode;
import org.jboss.pnc.common.graph.GraphStructureException;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.coordinator.RemoteBuildTask;
import org.jboss.pnc.spi.exception.BuildRequestException;
import org.jboss.util.graph.Graph;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class ExplicitDependenciesTest extends AbstractDependentBuildTest {

    private BuildConfiguration a, b, c, d, e, f, g, h;
    private BuildConfiguration[] all;

    @Before
    public void setUp() {

        // given
        d = config("d");
        c = config("c");
        b = config("b", d);
        a = config("a", b);
        all = new BuildConfiguration[] { d, c, b, a };
        insertNewBuildRecords(all);

        makeResult(b).dependOn(c);
        makeResult(a).dependOn(b, c);
    }

    @Test
    public void shouldBuildAOnModifiedB() throws GraphStructureException, BuildRequestException {
        // when
        insertNewBuildRecords(b);

        BuildOptions buildOptions = new BuildOptions();
        buildOptions.setRebuildMode(RebuildMode.EXPLICIT_DEPENDENCY_CHECK);
        Graph<RemoteBuildTask> buildGraph = createGraph(a, buildOptions);

        // then
        expectToBuildBuiltTask(buildGraph, a);
    }

    @Test
    public void shouldBuildAWhenNoDependencyOption() throws GraphStructureException, BuildRequestException {
        insertNewBuildRecords(b);

        // when
        BuildOptions buildOptions = new BuildOptions();
        buildOptions.setRebuildMode(RebuildMode.EXPLICIT_DEPENDENCY_CHECK);
        buildOptions.setBuildDependencies(false);
        Graph<RemoteBuildTask> buildGraph = createGraph(a, buildOptions);

        // then
        expectToBuildBuiltTask(buildGraph, a);
    }

    @Test
    public void shouldBuildABOnModifiedD() throws GraphStructureException, BuildRequestException {
        // when
        insertNewBuildRecords(d);

        BuildOptions buildOptions = new BuildOptions();
        buildOptions.setRebuildMode(RebuildMode.EXPLICIT_DEPENDENCY_CHECK);
        Graph<RemoteBuildTask> buildGraph = createGraph(a, buildOptions);

        // then
        expectToBuildBuiltTask(buildGraph, a, b);
    }

    @Test
    public void shouldNotBuildAOnModifiedC() throws GraphStructureException, BuildRequestException {
        // when
        insertNewBuildRecords(c);

        BuildOptions buildOptions = new BuildOptions();
        buildOptions.setRebuildMode(RebuildMode.EXPLICIT_DEPENDENCY_CHECK);
        Graph<RemoteBuildTask> buildGraph = createGraph(a, buildOptions);

        // then
        expectToBuildBuiltTask(buildGraph);
    }

    @Test
    public void shouldBuildAOnModifiedCWhenImplicitDependencyCheck()
            throws GraphStructureException, BuildRequestException {
        // when
        insertNewBuildRecords(c);

        BuildOptions buildOptions = new BuildOptions();
        buildOptions.setBuildDependencies(false);
        buildOptions.setRebuildMode(RebuildMode.IMPLICIT_DEPENDENCY_CHECK);
        Graph<RemoteBuildTask> buildGraph = createGraph(a, buildOptions);

        // then
        expectToBuildBuiltTask(buildGraph, a);
    }

    @Test
    public void shouldBuildABCOnForceAWithDependencies() throws GraphStructureException, BuildRequestException {
        // when
        insertNewBuildRecords(d, b, a);

        BuildOptions buildOptions = new BuildOptions();
        buildOptions.setBuildDependencies(true);
        buildOptions.setRebuildMode(RebuildMode.FORCE);
        Graph<RemoteBuildTask> buildGraph = createGraph(a, buildOptions);

        // then
        expectToBuildBuiltTask(buildGraph, d, b, a);
    }
}
