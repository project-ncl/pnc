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
package org.jboss.pnc.coordinator.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2015-01-06.
 */
@RunWith(Arquillian.class)
public class MultipleProjectsBuildTest extends ProjectBuilder {

    @Deployment
    public static JavaArchive createDeployment() {
        return BuildCoordinatorDeployments.deployment(
                BuildCoordinatorDeployments.Options.WITH_DATASTORE,
                BuildCoordinatorDeployments.Options.WITH_BPM);
    }

    private static final Logger log = Logger.getLogger(MultipleProjectsBuildTest.class.getName());
    private final int N_PROJECTS = 100;

    @Inject
    BuildCoordinatorFactory buildCoordinatorFactory;

    @Test
    @InSequence(10)
    public void buildMultipleProjectsTestCase() throws Exception {
        log.info("Start multiple projects build test.");
        long startTime = System.currentTimeMillis();

        List<Runnable> list = new ArrayList<>();
        for (int i = 0; i < N_PROJECTS; i++) { // create N project configurations
            Integer id = i;
            list.add(() -> {
                try {
                    // noinspection deprecation
                    buildProject(
                            configurationBuilder.build(id, "c" + id + "-java"),
                            buildCoordinatorFactory.createBuildCoordinator(datastore).coordinator);
                    clearSemaphores();
                } catch (Exception e) {
                    throw new AssertionError(e);
                }
            });
        }

        Function<Runnable, Thread> runInNewThread = (r) -> {
            Thread t = new Thread(r, "test-build");
            t.start();
            return t;
        };

        Consumer<Thread> waitToComplete = (t) -> {
            try {
                log.fine("Waiting thread to complete: " + t);
                t.join(30000);
            } catch (InterruptedException e) {
                throw new AssertionError("Interrupted while waiting threads to complete", e);
            }
        };

        // List<Thread> threads = list.stream().map(runInNewThread).collect(Collectors.toList());
        // threads.forEach(waitToComplete);
        list.forEach(Runnable::run); // TODO re-enable parallel builds

        log.info("Completed multiple projects build test in " + (System.currentTimeMillis() - startTime) + "ms.");
    }

    @Test
    @InSequence(20)
    public void checkDatabaseForResult() {
        List<BuildRecord> buildRecords = datastore.getBuildRecords();
        Assert.assertEquals("Wrong datastore results count.", N_PROJECTS, buildRecords.size());

        BuildRecord buildRecord = buildRecords.get(0);
        BuildStatus status = buildRecord.getStatus();
        Assert.assertEquals("Invalid build status: " + status, BuildStatus.SUCCESS, status);

        assertArtifactsPresent(buildRecord.getBuiltArtifacts());
        assertArtifactsPresent(buildRecord.getDependencies());
    }

}
