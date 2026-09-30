/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.remotecoordinator.builder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.coordinator.RemoteBuildTask;
import org.jboss.pnc.spi.exception.BuildConflictException;
import org.jboss.util.graph.Graph;
import org.jboss.util.graph.Vertex;
import org.junit.Assert;
import org.junit.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GraphValidationTest {

    @Test
    public void shouldThrowIfAnyDependencyOfAlreadyRunningIsSubmitted() {

        // given
        BuildConfiguration bc1 = BuildConfiguration.Builder.newBuilder().name("bc1").build();
        BuildConfigurationAudited bca1 = BuildConfigurationAudited.Builder.newBuilder().buildConfiguration(bc1).build();
        RemoteBuildTask running = new RemoteBuildTask(
                "1",
                Instant.now().minus(1, ChronoUnit.MINUTES),
                bca1,
                new BuildOptions(),
                "1",
                true,
                null,
                null,
                new ArrayList<>(),
                new ArrayList<>());

        BuildConfiguration bc2 = BuildConfiguration.Builder.newBuilder().name("bc2").build();
        BuildConfigurationAudited bca2 = BuildConfigurationAudited.Builder.newBuilder().buildConfiguration(bc2).build();
        RemoteBuildTask submitted = new RemoteBuildTask(
                "2",
                Instant.now(),
                bca2,
                new BuildOptions(),
                "1",
                false,
                null,
                null,
                new ArrayList<>(),
                new ArrayList<>());

        Vertex<RemoteBuildTask> runningVertex = new Vertex<>(running.getId(), running);
        Vertex<RemoteBuildTask> submittedVertex = new Vertex<>(submitted.getId(), submitted);

        Graph<RemoteBuildTask> buildGraph = new Graph<>();
        buildGraph.addVertex(runningVertex);
        buildGraph.addVertex(submittedVertex);
        buildGraph.addEdge(runningVertex, submittedVertex, 1);

        try {
            GraphValidation.checkIfAnyDependencyOfAlreadyRunningIsSubmitted(buildGraph);
            Assert.fail("Validation should thrown an exception.");
        } catch (Exception e) {
            log.info("Exception:", e);
            Assert.assertTrue(e instanceof BuildConflictException);
        }

    }
}
