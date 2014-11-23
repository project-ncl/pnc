package org.jboss.pnc.core.test;

import org.jboss.pnc.core.Builder;
import org.jboss.pnc.core.model.BuildType;
import org.jboss.pnc.core.model.Project;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
public class BuildProjectsTestCase {

    @Test
    public void createProjectStructure() {
        Project p1 = new Project("p1-native", BuildType.NATIVE);
        Project p2 = new Project("p2-java", BuildType.JAVA, p1);
        Project p3 = new Project("p3-java", BuildType.JAVA);
        Project p4 = new Project("p4-java", BuildType.JAVA, p2, p3);
        Project p5 = new Project("p5-docker", BuildType.DOCKER, p4);
        Project p6 = new Project("p6-java", BuildType.JAVA);

        HashSet<Project> projects = new HashSet<Project>(Arrays.asList(new Project[]{p5, p6}));

        Builder builder = new Builder();
        builder.buildProjects(projects);
    }


}
