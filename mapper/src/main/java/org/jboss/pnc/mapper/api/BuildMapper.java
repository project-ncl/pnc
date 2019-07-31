/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.mapper.api;

import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfigurationRevisionRef;
import org.jboss.pnc.dto.BuildRef;
import org.jboss.pnc.dto.ProjectRef;
import org.jboss.pnc.enums.BuildCoordinationStatus;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.mapper.BrewNameWorkaround;
import org.jboss.pnc.mapper.api.BuildMapper.StatusMapper;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Mapper(config = MapperCentralConfig.class,
        uses = {BuildConfigurationMapper.class, UserMapper.class, StatusMapper.class, BuildMapper.IDMapper.class,
                SCMRepositoryMapper.class, ProjectMapper.class, BuildConfigurationRevisionMapper.class,
                EnvironmentMapper.class, BuildMapper.BuildTaskIdMapper.class, BrewNameWorkaround.class,
                GroupBuildMapper.class})

public interface BuildMapper extends EntityMapper<BuildRecord, Build, BuildRef> {

    @Override
    @Mapping(target = "environment", source = "buildConfigurationAudited.buildEnvironment", qualifiedBy = Reference.class)
    @Mapping(target = "dependentBuildIds", source = "dependentBuildRecordIds")
    @Mapping(target = "dependencyBuildIds", source = "dependencyBuildRecordIds")
    @Mapping(target = "buildConfigRevision", source = "buildConfigurationAudited", resultType = BuildConfigurationRevisionRef.class)
    @Mapping(target = "project", source = "buildConfigurationAudited.project", resultType = ProjectRef.class)
    @Mapping(target = "scmRepository", source = "buildConfigurationAudited.repositoryConfiguration", qualifiedBy = Reference.class)
    @Mapping(target = "groupBuild", source = "buildConfigSetRecord", qualifiedBy = Reference.class)
    @Mapping(target = "user", qualifiedBy = Reference.class)
    @Mapping(target = "scmRepositoryURL", source = "scmRepoURL")
    @Mapping(target = "attributes", ignore = true)
    @BeanMapping(ignoreUnmappedSourceProperties = {"scmRevision", "scmTag", "buildLog", "buildLogMd5", "buildLogSha256",
            "buildLogSize", "sshCommand", "sshPassword", "executionRootName", "executionRootVersion", "builtArtifacts",
            "dependencies", "productMilestone", "repourLog", "repourLogMd5", "repourLogSha256",
            "repourLogSize", "buildRecordPushResults", "buildConfigurationId", "buildConfigurationRev",
            "buildConfigurationAuditedIdRev", "buildEnvironment"
    })
    Build toDTO(BuildRecord dbEntity);

    @Override
    default BuildRecord toIDEntity(BuildRef dtoEntity) {
        if (dtoEntity == null) {
            return null;
        }
        BuildRecord entity = new BuildRecord();
        entity.setId(dtoEntity.getId());
        return entity;
    }

    @Override
    @Mapping(target = "scmRepositoryURL", source = "scmRepoURL")
    @BeanMapping(ignoreUnmappedSourceProperties = {"scmRevision", "scmTag", "buildLog", "buildLogMd5", "buildLogSha256",
            "buildLogSize", "sshCommand", "sshPassword", "executionRootName", "executionRootVersion", "builtArtifacts",
            "dependencies", "productMilestone", "buildConfigSetRecord", "repourLog", "repourLogMd5", "repourLogSha256",
            "repourLogSize", "buildRecordPushResults", "buildConfigurationId", "buildConfigurationRev", "buildEnvironment",
            "buildConfigurationAudited" ,"dependentBuildRecordIds", "dependencyBuildRecordIds", "user", "attributes",
            "buildConfigurationAuditedIdRev"
    })
    BuildRef toRef(BuildRecord dbEntity);
    
    @Override
    @Mapping(target = "buildEnvironment", source = "environment", qualifiedBy = IdEntity.class)
    @Mapping(target = "dependentBuildRecordIds", source = "dependentBuildIds")
    @Mapping(target = "dependencyBuildRecordIds", source = "dependencyBuildIds")
    @Mapping(target = "buildConfigurationAudited", source = "buildConfigRevision")
    @Mapping(target = "buildConfigSetRecord", source = "groupBuild")
    @Mapping(target = "scmRepoURL", source = "scmRepositoryURL")
    @Mapping(target = "user", qualifiedBy = IdEntity.class)
    @Mapping(target = "scmRevision", ignore = true)
    @Mapping(target = "scmTag", ignore = true)
    @Mapping(target = "repourLog", ignore = true)
    @Mapping(target = "buildLog", ignore = true)
    @Mapping(target = "builtArtifacts", ignore = true)
    @Mapping(target = "dependencies", ignore = true)
    @Mapping(target = "buildConfigurationId", ignore = true)
    @Mapping(target = "buildConfigurationRev", ignore = true)
    @Mapping(target = "productMilestone", ignore = true)
    @Mapping(target = "buildLogMd5", ignore = true)
    @Mapping(target = "buildLogSha256", ignore = true)
    @Mapping(target = "buildLogSize", ignore = true)
    @Mapping(target = "sshCommand", ignore = true)
    @Mapping(target = "sshPassword", ignore = true)
    @Mapping(target = "executionRootName", ignore = true)
    @Mapping(target = "executionRootVersion", ignore = true)
    @Mapping(target = "repourLogMd5", ignore = true)
    @Mapping(target = "repourLogSha256", ignore = true)
    @Mapping(target = "repourLogSize", ignore = true)
    @Mapping(target = "buildRecordPushResults", ignore = true)
    @Mapping(target = "attributes", ignore = true)
    @BeanMapping(ignoreUnmappedSourceProperties = {"project", "scmRepository"})
    BuildRecord toEntity(Build dtoEntity);



    @Mapping(target = "project", source = "buildConfigurationAudited.project", resultType = ProjectRef.class)
    @Mapping(target = "scmRepository", source = "buildConfigurationAudited.repositoryConfiguration", qualifiedBy = Reference.class)
    @Mapping(target = "environment", source = "buildConfigurationAudited.buildEnvironment", qualifiedBy = Reference.class)
    @Mapping(target = "buildConfigRevision", source = "buildConfigurationAudited", resultType = BuildConfigurationRevisionRef.class)
    //Workaround for [NCL-4228]
    //Use of Reference class was needed here because resultType=GroupBuildRef.class along with unwrapping of Optional resulted in NPE in Mapstruct processor
    @Mapping(target = "groupBuild", source = "buildSetTask.buildConfigSetRecord", qualifiedBy = Reference.class)
    @Mapping(target = "dependentBuildIds", source = "dependants")
    @Mapping(target = "dependencyBuildIds", source = "dependencies")
    @Mapping(target = "buildContentId", source = "contentId")
    @Mapping(target = "temporaryBuild", source = "buildOptions.temporaryBuild")
    @Mapping(target = "scmRepositoryURL", ignore = true)
    @Mapping(target = "attributes", ignore = true)
    @BeanMapping(ignoreUnmappedSourceProperties = {
            "productMilestone", "statusDescription", "buildSetTask", "buildConfigSetRecordId", "buildOptions"})
    Build fromBuildTask(BuildTask buildTask);

    public static <T> T unwrap(Optional<T> optional) {
        return (optional != null && optional.isPresent()) ? optional.get() : null;
    }

    public static class StatusMapper {
        public static BuildCoordinationStatus toBuildCoordinationStatus(BuildStatus status) {
            return BuildCoordinationStatus.fromBuildStatus(status);
        }

        public static BuildStatus toBuildStatus(BuildCoordinationStatus status) {
            return BuildStatus.fromBuildCoordinationStatus(status);
        }
    }

    public static class IDMapper {
        public static BuildRecord toIdEntity(Integer id) {
            BuildRecord buildRecord = new BuildRecord();
            buildRecord.setId(id);
            return buildRecord;
        }

        public static Integer toId(BuildRecord buildRecord){
            return buildRecord.getId();
        }
    }

    public static class BuildTaskIdMapper {
        public static List<Integer> toBuildIds(Set<BuildTask> buildTasks) {
            return buildTasks.stream()
                    .map(BuildTask::getId)
                    .collect(Collectors.toList());
        }
    }
}
