package org.jboss.pnc.core.test.buildCoordinator;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.core.builder.BuildSetTask;
import org.jboss.pnc.core.builder.BuildTasksTree;
import org.jboss.pnc.core.test.configurationBuilders.TestProjectConfigurationBuilder;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.spi.BuildExecutionType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2015-01-06.
 */
@RunWith(Arquillian.class)
public class ReadDependenciesTest extends ProjectBuilder {

    @Test
    public void createDependencyTreeTestCase() {
        TestProjectConfigurationBuilder configurationBuilder = new TestProjectConfigurationBuilder();
        BuildConfigurationSet buildConfigurationSet = configurationBuilder.buildConfigurationSet();
        BuildSetTask buildSetTask = new BuildSetTask(buildConfigurationSet, BuildExecutionType.COMPOSED_BUILD);
        BuildTasksTree buildTasksTree = BuildTasksTree.newInstance(buildCoordinator, buildSetTask);

        Assert.assertEquals("Missing projects in tree structure.", 5, buildTasksTree.getBuildTasks().size());
    }
}
