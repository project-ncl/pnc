package org.jboss.pnc.core.test.buildCoordinator;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.core.builder.BuildTask;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.core.test.configurationBuilders.TestBuildRecordSetBuilder;
import org.jboss.pnc.core.test.configurationBuilders.TestProjectConfigurationBuilder;
import org.jboss.pnc.model.BuildRecordSet;
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
        BuildRecordSet buildRecordSet = new TestBuildRecordSetBuilder().build("foo", "Foo desc.", "1.0");
        TestProjectConfigurationBuilder configurationBuilder = new TestProjectConfigurationBuilder();

        Function<TestBuildConfig, Runnable> createJob = (config) -> {
            Runnable task = () -> {
                try {
                    buildProject(config.configuration, config.collection);
                } catch (InterruptedException | CoreException e) {
                    throw new AssertionError("Something went wrong.", e);
                }
            };
            return task;
        };

        List<Runnable> list = new ArrayList();
        for (int i = 0; i < 100; i++) { //create 100 project configurations
            list.add(createJob.apply(new TestBuildConfig(configurationBuilder.build(i, "c" + i + "-java"), buildRecordSet)));
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
