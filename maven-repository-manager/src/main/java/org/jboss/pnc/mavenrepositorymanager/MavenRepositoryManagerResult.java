package org.jboss.pnc.mavenrepositorymanager;

import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;

import java.util.List;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2015-02-02.
 */
public class MavenRepositoryManagerResult implements RepositoryManagerResult {
    private final List<Artifact> builtArtifacts;
    private final List<Artifact> dependencies;

    public MavenRepositoryManagerResult(List<Artifact> builtArtifacts, List<Artifact> dependencies) {
        this.builtArtifacts = builtArtifacts;
        this.dependencies = dependencies;
    }

    @Override
    public List<Artifact> getBuiltArtifacts() {
        return builtArtifacts;
    }

    @Override
    public List<Artifact> getDependencies() {
        return dependencies;
    }
}
