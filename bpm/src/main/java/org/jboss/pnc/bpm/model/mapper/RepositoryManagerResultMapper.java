package org.jboss.pnc.bpm.model.mapper;

import org.jboss.pnc.bpm.model.RepositoryManagerResultRest;
import org.jboss.pnc.bpm.model.RepositoryManagerResultRest.GenericRepositoryManagerResult;
import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.mapper.api.ArtifactMapper;
import org.jboss.pnc.spi.coordinator.CompletionStatus;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class RepositoryManagerResultMapper {

    @Inject
    private ArtifactMapper artifactMapper;

    public RepositoryManagerResultRest toDTO(RepositoryManagerResult entity) {
        List<Artifact> builtArtifacts = entity.getBuiltArtifacts().stream().map(artifact -> artifactMapper.toDTO(artifact)).collect(Collectors.toList());
        List<Artifact> dependencies = entity.getDependencies().stream().map(artifact -> artifactMapper.toDTO(artifact)).collect(Collectors.toList());
        String buildContentId = entity.getBuildContentId();
        String log = entity.getLog();
        CompletionStatus completionStatus = entity.getCompletionStatus();
        return new RepositoryManagerResultRest(builtArtifacts, dependencies, buildContentId, log, completionStatus);
    }

    public RepositoryManagerResult toEntity(RepositoryManagerResultRest dto) {
        List<org.jboss.pnc.model.Artifact> builtArtifacts = dto.getBuiltArtifacts().stream().map(artifactRest -> artifactMapper.toEntity(artifactRest)).collect(Collectors.toList());
        List<org.jboss.pnc.model.Artifact> dependencies = dto.getDependencies().stream().map(artifactRest -> artifactMapper.toEntity(artifactRest)).collect(Collectors.toList());
        String buildContentId = dto.getBuildContentId();
        String log = dto.getLog();
        CompletionStatus completionStatus = dto.getCompletionStatus();
        return new GenericRepositoryManagerResult(builtArtifacts, dependencies, buildContentId, log, completionStatus);
    }
}
