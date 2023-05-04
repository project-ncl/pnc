/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    private ArtifactMapper artifactMapper;

    // CDI
    public RepositoryManagerResultMapper() {
    }

    @Inject
    public RepositoryManagerResultMapper(ArtifactMapper artifactMapper) {
        this.artifactMapper = artifactMapper;
    }

    public RepositoryManagerResultRest toDTO(RepositoryManagerResult entity) {
        List<Artifact> builtArtifacts = entity.getBuiltArtifacts()
                .stream()
                .map(artifact -> artifactMapper.toDTO(artifact))
                .collect(Collectors.toList());
        List<Artifact> dependencies = entity.getDependencies()
                .stream()
                .map(artifact -> artifactMapper.toDTO(artifact))
                .collect(Collectors.toList());
        String buildContentId = entity.getBuildContentId();
        String log = entity.getLog();
        CompletionStatus completionStatus = entity.getCompletionStatus();
        return new RepositoryManagerResultRest(builtArtifacts, dependencies, buildContentId, log, completionStatus);
    }

    public RepositoryManagerResult toEntity(RepositoryManagerResultRest dto) {
        List<org.jboss.pnc.model.Artifact> builtArtifacts = dto.getBuiltArtifacts()
                .stream()
                .map(artifactRest -> artifactMapper.toEntityWithTransientTargetRepository(artifactRest))
                .collect(Collectors.toList());
        List<org.jboss.pnc.model.Artifact> dependencies = dto.getDependencies()
                .stream()
                .map(artifactRest -> artifactMapper.toEntityWithTransientTargetRepository(artifactRest))
                .collect(Collectors.toList());
        String buildContentId = dto.getBuildContentId();
        String log = dto.getLog();
        CompletionStatus completionStatus = dto.getCompletionStatus();
        return new GenericRepositoryManagerResult(builtArtifacts, dependencies, buildContentId, log, completionStatus);
    }
}
