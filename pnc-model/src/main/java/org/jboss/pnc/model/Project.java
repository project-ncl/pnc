package org.jboss.pnc.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
public class Project {

    private String name;
    private String scmURL;
    private BuildType buildType;
    private Set<Project> dependencies;

//    public Project(String name, BuildType buildType) {
//        this.name = name;
//        this.buildType = buildType;
//    }

    public Project(String name, BuildType buildType, Project... dependencies) {
        this.name = name;
        this.buildType = buildType;
        this.dependencies = new HashSet<Project>(Arrays.asList(dependencies));
    }

    public BuildType getBuildType() {
        return buildType;
    }

    public Set<Project> getDependencies() {
        return dependencies;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
