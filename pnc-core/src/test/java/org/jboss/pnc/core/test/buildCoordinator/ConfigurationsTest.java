package org.jboss.pnc.core.test.buildCoordinator;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.core.builder.BuildCoordinator;
import org.jboss.pnc.core.builder.BuildTask;
import org.jboss.pnc.core.test.configurationBuilders.TestProjectConfigurationBuilder;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.spi.BuildStatus;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.function.Consumer;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
@RunWith(Arquillian.class)
public class ConfigurationsTest extends ProjectBuilder {

    @Inject
    BuildCoordinator buildCoordinator;

    @Test
    @InSequence(10)
    public void dependsOnItselfConfigurationTestCase() throws Exception {
        TestProjectConfigurationBuilder configurationBuilder = new TestProjectConfigurationBuilder();

        BuildConfiguration buildConfiguration = configurationBuilder.buildConfigurationWhichDependsOnItself();
        BuildTask buildTask = buildCoordinator.build(buildConfiguration, new HashSet<>(), new HashSet<Consumer<String>>());
        Assert.assertEquals(BuildStatus.REJECTED, buildTask.getStatus());
        Assert.assertTrue("Invalid status description: " + buildTask.getStatusDescription(), buildTask.getStatusDescription().contains("itself"));
    }

    @Test
    @InSequence(15)
    public void cycleConfigurationTestCase() throws Exception {
        TestProjectConfigurationBuilder configurationBuilder = new TestProjectConfigurationBuilder();

        BuildConfiguration buildConfiguration = configurationBuilder.buildConfigurationWithCycleDependency();
        BuildTask buildTask = buildCoordinator.build(buildConfiguration, new HashSet<>(), new HashSet<Consumer<String>>());
        Assert.assertEquals(BuildStatus.REJECTED, buildTask.getStatus());
        Assert.assertTrue("Invalid status description: " + buildTask.getStatusDescription(), buildTask.getStatusDescription().contains("Cycle dependencies found"));
    }

}
