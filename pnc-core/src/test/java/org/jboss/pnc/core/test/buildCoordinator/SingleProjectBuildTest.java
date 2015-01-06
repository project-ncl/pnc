package org.jboss.pnc.core.test.buildCoordinator;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.core.test.configurationBuilders.TestBuildCollectionBuilder;
import org.jboss.pnc.core.test.configurationBuilders.TestProjectConfigurationBuilder;
import org.jboss.pnc.model.BuildCollection;
import org.jboss.pnc.model.ProjectBuildResult;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2015-01-06.
 */
@RunWith(Arquillian.class)
public class SingleProjectBuildTest extends ProjectBuilder {

    @Test
    @InSequence(10)
    public void buildSingleProjectTestCase() throws Exception {
        BuildCollection buildCollection = new TestBuildCollectionBuilder().build("foo", "Foo desc.", "1.0");
        TestProjectConfigurationBuilder configurationBuilder = new TestProjectConfigurationBuilder();
        buildProject(configurationBuilder.build(1, "c1-java"), buildCollection);
    }


    @Test
    @InSequence(20)
    public void checkDatabaseForResult() {
        List<ProjectBuildResult> buildResults = datastore.getBuildResults();
        Assert.assertTrue("Missing datastore results.", buildResults.size() == 1);

        ProjectBuildResult projectBuildResult = buildResults.get(0);
        String buildLog = projectBuildResult.getBuildLog();
        Assert.assertTrue("Invalid build log.", buildLog.contains("Finished: SUCCESS"));
    }
}
