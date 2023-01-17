/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.remotecoordinator.test.dependencies;

import org.jboss.pnc.common.graph.GraphStructureException;
import org.jboss.pnc.common.graph.GraphUtils;
import org.jboss.pnc.enums.BuildCoordinationStatus;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.coordinator.BuildTaskRef;
import org.jboss.pnc.spi.coordinator.DefaultBuildTaskRef;
import org.jboss.pnc.spi.coordinator.RemoteBuildTask;
import org.jboss.util.graph.Graph;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeoutException;

/**
 * Let A -> B, C denote that config A depends on configs B and C <br/>
 *
 *
 * <pre>
 *     A -> B, C
 *     B -> D, E, F
 *     C -> D. G
 *     G -> H
 * </pre>
 *
 *
 * <br>
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 11/30/16 Time: 3:57 PM
 */
public class TransitiveDependenciesTest extends AbstractDependentBuildTest {

    private BuildConfiguration a, b, c, d, e, f, g, h;
    private BuildConfiguration[] all;

    @Before
    public void setUp() throws TimeoutException, InterruptedException {
        // given
        h = config("h");
        g = config("g", h);
        f = config("f");
        e = config("e");
        d = config("d");
        c = config("c", d, g);
        b = config("b", d, e, f);
        a = config("a", b, c);
        all = new BuildConfiguration[] { h, g, f, e, d, c, b, a };
        insertNewBuildRecords(all);

        makeResult(g).dependOn(h);
        makeResult(c).dependOn(d, g);
        makeResult(b).dependOn(d, d, e, f);
        makeResult(a).dependOn(b, c);
    }

    @Test
    public void shouldBuildAOnModifiedA() throws GraphStructureException {
        // when
        modifyConfigurations(a);
        Graph<RemoteBuildTask> buildGraph = createGraph(a);

        // then
        expectBuiltTask(buildGraph, a);
    }

    @Test
    public void shouldBuildABCDOnModifiedD() throws GraphStructureException {
        // when
        modifyConfigurations(d);
        Graph<RemoteBuildTask> buildGraph = createGraph(a);

        // then
        expectBuiltTask(buildGraph, a, b, c, d);
    }

    @Test
    public void shouldBuildABEOnModifiedE() throws GraphStructureException {
        // when
        modifyConfigurations(e);
        Graph<RemoteBuildTask> buildGraph = createGraph(a);
        // then
        expectBuiltTask(buildGraph, a, b, e);
    }

    @Test
    public void shouldBuildACGHOnModifiedH() throws GraphStructureException {
        // when
        modifyConfigurations(h);
        Graph<RemoteBuildTask> buildGraph = createGraph(a);
        // then
        expectBuiltTask(buildGraph, a, c, g, h);
    }

    @Test
    public void shouldNotBuildWithoutModifications() throws GraphStructureException {
        // when
        Graph<RemoteBuildTask> buildGraph = createGraph(a);
        // then
        expectBuiltTask(buildGraph);
    }

    @Test
    public void shouldBuildAllOnAllModified() throws GraphStructureException {
        // when
        modifyConfigurations(all);
        Graph<RemoteBuildTask> buildGraph = createGraph(a);
        // then
        expectBuiltTask(buildGraph, all);
    }

    @Test
    public void shouldBuildAllOnModifiedDEFH() throws GraphStructureException {
        // when
        modifyConfigurations(d, e, f, h);
        Graph<RemoteBuildTask> buildGraph = createGraph(a);
        // then
        expectBuiltTask(buildGraph, all);
    }

    @Test
    public void shouldLinkToRunningTask() throws GraphStructureException {
        // when
        modifyConfigurations(b);
        BuildConfigurationAudited auditedA = datastoreAdapter
                .getLatestBuildConfigurationAuditedInitializeBCDependencies(a.getId());
        BuildConfigurationAudited auditedB = datastoreAdapter
                .getLatestBuildConfigurationAuditedInitializeBCDependencies(b.getId());

        BuildTaskRef taskRefB = new DefaultBuildTaskRef(
                auditedB.getId()+"",
                auditedB.getIdRev(),
                "",
                "",
                "",
                Instant.now(),
                BuildCoordinationStatus.BUILDING);
        Collection<BuildTaskRef> alreadyRunning = Set.of(taskRefB);
        Graph<RemoteBuildTask> buildGraph = buildTasksInitializer.createBuildGraph(
                auditedA,
                user,
                new BuildOptions(),
                alreadyRunning);

        // then
        Collection<RemoteBuildTask> remoteBuildTasks = GraphUtils.unwrap(buildGraph.getVerticies());
        long runningCount = remoteBuildTasks.stream().filter(RemoteBuildTask::isAlreadyRunning).count();
        Assert.assertEquals(1, runningCount);

        RemoteBuildTask runningB = remoteBuildTasks.stream()
                .filter(t -> t.getBuildConfigurationAudited().getName().equals("b"))
                .findAny()
                .orElseThrow(() -> new AssertionError("Missing B."));
        Assert.assertTrue(runningB.isAlreadyRunning());

        expectBuiltTask(buildGraph, a, b);
    }
}
