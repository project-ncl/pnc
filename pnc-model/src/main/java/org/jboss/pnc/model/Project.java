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
    private Environment environment;
    private Set<Project> dependencies;

    public Project(String name, Environment environment, Project... dependencies) {
        this.name = name;
        this.environment = environment;
        this.dependencies = new HashSet<Project>(Arrays.asList(dependencies));
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

    public Environment getEnvironment() {
        return environment;
    }
}
