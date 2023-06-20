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
package org.jboss.pnc.remotecoordinator.builder;

import lombok.extern.slf4j.Slf4j;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.coordinator.RemoteBuildTask;
import org.jboss.pnc.spi.exception.BuildConflictException;
import org.jboss.util.graph.Graph;
import org.jboss.util.graph.Vertex;
import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

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
