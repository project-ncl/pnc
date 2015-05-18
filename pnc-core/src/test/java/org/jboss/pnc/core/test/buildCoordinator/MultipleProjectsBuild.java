/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.core.test.buildCoordinator;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.core.builder.BuildTask;
import org.jboss.pnc.core.test.configurationBuilders.TestProjectConfigurationBuilder;
import org.jboss.pnc.model.BuildRecord;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2015-01-06.
 */
@RunWith(Arquillian.class)
public class MultipleProjectsBuild extends ProjectBuilder {

    private static final Logger log = Logger.getLogger(MultipleProjectsBuild.class.getName());

    @Test
    @InSequence(10)
    public void buildMultipleProjectsTestCase() throws Exception {
        log.info("Start multiple projects build test.");
        long startTime = System.currentTimeMillis();

        TestProjectConfigurationBuilder configurationBuilder = new TestProjectConfigurationBuilder();

        List<Runnable> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) { //create 100 project configurations
            buildProject(configurationBuilder.build(i, "c" + i + "-java"));
        }

        Function<Runnable, Thread> runInNewThread = (r) -> {
            Thread t = new Thread(r);
            t.start();
            return t;
        };

        Consumer<Thread> waitToComplete = (t) -> {
            try {
                t.join(30000);
            } catch (InterruptedException e) {
                throw new AssertionError("Interrupted while waiting threads to complete", e);
            }
        };

        List<Thread> threads = list.stream().map(runInNewThread).collect(Collectors.toList());

        Assert.assertTrue("There are no running builds.", buildCoordinator.getBuildTasks().size() > 0);
        BuildTask buildTask = buildCoordinator.getBuildTasks().iterator().next();
        Assert.assertTrue("Build has no status.", buildTask.getStatus() != null);

        threads.forEach(waitToComplete);
        log.info("Completed multiple projects build test in " + (System.currentTimeMillis() - startTime) + "ms.");
    }

    @Test
    @InSequence(20)
    public void checkDatabaseForResult() {  //TODO datastore task is waiting all build to complete see BuildCoordinator::startBuilding
        List<BuildRecord> buildRecords = datastore.getBuildRecords();
        Assert.assertEquals("Wrong datastore results count.", 100, buildRecords.size());

        BuildRecord buildRecord = buildRecords.get(0);
        String buildLog = buildRecord.getBuildLog();
        Assert.assertTrue("Invalid build log.", buildLog.contains("Finished: SUCCESS"));

        assertBuildArtifactsPresent(buildRecord.getBuiltArtifacts());
        assertBuildArtifactsPresent(buildRecord.getDependencies());
    }


}
