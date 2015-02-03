package org.jboss.pnc.spi.repositorymanager;

import org.jboss.pnc.model.Artifact;

import java.util.List;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2015-02-02.
 */
public interface RepositoryManagerResult {
    List<Artifact> getBuiltArtifacts();

    List<Artifact> getDependencies();
}
