package org.jboss.pnc.core.test.buildCoordinator;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.core.test.configurationBuilders.TestBuildRecordSetBuilder;
import org.jboss.pnc.core.test.configurationBuilders.TestProjectConfigurationBuilder;
import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.model.BuildRecord;
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
        BuildRecordSet buildRecordSet = new TestBuildRecordSetBuilder().build("foo", "Foo desc.", "1.0");
        TestProjectConfigurationBuilder configurationBuilder = new TestProjectConfigurationBuilder();
        buildProject(configurationBuilder.build(1, "c1-java"), buildRecordSet);
    }


    @Test
    @InSequence(20)
    public void checkDatabaseForResult() {
        List<BuildRecord> buildRecords = datastore.getBuildRecords();
        Assert.assertEquals("Wrong datastore results count.", 1, buildRecords.size());

        BuildRecord buildRecord = buildRecords.get(0);
        String buildLog = buildRecord.getBuildLog();
        Assert.assertTrue("Invalid build log.", buildLog.contains("Finished: SUCCESS"));

        assertBuildArtifactsPresent(buildRecord.getBuiltArtifacts());
        assertBuildArtifactsPresent(buildRecord.getDependencies());
    }
}
