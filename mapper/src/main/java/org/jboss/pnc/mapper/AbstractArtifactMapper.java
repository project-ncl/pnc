/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.mapper;

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.util.UrlUtils;
import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.ArtifactRef;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.mapper.api.ArtifactMapper;
import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.mapper.api.IdEntity;
import org.jboss.pnc.mapper.api.MapperCentralConfig;
import org.jboss.pnc.mapper.api.Reference;
import org.jboss.pnc.mapper.api.TargetRepositoryMapper;
import org.jboss.pnc.mapper.api.UserMapper;
import org.jboss.pnc.model.TargetRepository;
import org.mapstruct.BeanMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import java.net.MalformedURLException;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:jmichalo@redhat.com">Jan Michalov</a>
 */
@Mapper(
        config = MapperCentralConfig.class,
        uses = {
                TargetRepositoryMapper.class,
                BuildMapper.IDMapper.class,
                Configuration.class,
                BuildMapper.class,
                UserMapper.class })
public abstract class AbstractArtifactMapper implements ArtifactMapper {

    private static final Logger logger = LoggerFactory.getLogger(AbstractArtifactMapper.class);

    @Inject
    private Configuration config;

    @Override
    @Mapping(target = "deployUrl", ignore = true)
    @Mapping(target = "publicUrl", ignore = true)
    @Mapping(target = "build", source = "buildRecord")
    @Mapping(target = "targetRepository", qualifiedBy = Reference.class)
    @Mapping(target = "creationUser", qualifiedBy = Reference.class)
    @Mapping(target = "modificationUser", qualifiedBy = Reference.class)
    @BeanMapping(
            ignoreUnmappedSourceProperties = {
                    "distributedInProductMilestones",
                    "identifierSha256",
                    "built",
                    "imported",
                    "trusted",
                    "descriptiveString",
                    "dependantBuildRecords" })
    public abstract Artifact toDTO(org.jboss.pnc.model.Artifact dbEntity);

    @Override
    @Mapping(target = "deployUrl", ignore = true)
    @Mapping(target = "publicUrl", ignore = true)
    @BeanMapping(
            ignoreUnmappedSourceProperties = {
                    "targetRepository",
                    "buildRecords",
                    "dependantBuildRecords",
                    "importDate",
                    "distributedInProductMilestones",
                    "identifierSha256",
                    "built",
                    "imported",
                    "trusted",
                    "descriptiveString",
                    "creationUser",
                    "modificationUser" })
    public abstract ArtifactRef toRef(org.jboss.pnc.model.Artifact dbEntity);

    @Override
    @Mapping(target = "creationUser", qualifiedBy = IdEntity.class)
    @Mapping(target = "modificationUser", qualifiedBy = IdEntity.class)
    @Mapping(target = "buildRecord", source = "build", qualifiedBy = IdEntity.class)
    @Mapping(target = "dependantBuildRecords", ignore = true)
    /*
     * Builder that MapStruct uses when generating mapper has method dependantBuildRecord() which confuses MapStruct as
     * he thinks it is a new property
     */
    @Mapping(target = "dependantBuildRecord", ignore = true)
    @Mapping(target = "distributedInProductMilestones", ignore = true)
    @BeanMapping(ignoreUnmappedSourceProperties = { "deployUrl", "publicUrl" })
    public abstract org.jboss.pnc.model.Artifact toEntity(Artifact dtoEntity);

    @BeforeMapping
    protected void fillDeployAndPublicUrl(
            org.jboss.pnc.model.Artifact artifactDB,
            @MappingTarget Artifact.Builder artifactDTO) {
        fillDeployAndPublicUrl(artifactDB, artifactDTO::deployUrl, artifactDTO::publicUrl);
    }

    @BeforeMapping
    void fillDeployAndPublicUrl(
            org.jboss.pnc.model.Artifact artifactDB,
            @MappingTarget ArtifactRef.Builder artifactREF) {
        fillDeployAndPublicUrl(artifactDB, artifactREF::deployUrl, artifactREF::publicUrl);
    }

    private void fillDeployAndPublicUrl(
            org.jboss.pnc.model.Artifact artifactDB,
            Consumer<String> deployUrlSetter,
            Consumer<String> publicUrlSetter) {
        GlobalModuleGroup globalConfig = null;
        try {
            globalConfig = config.getGlobalConfig();
        } catch (ConfigurationParseException e) {
            logger.error("Cannot read configuration", e);
        }
        if (globalConfig == null) {
            return;
        }
        TargetRepository targetRepository = artifactDB.getTargetRepository();
        if (targetRepository == null) {
            logger.error("Artifact DB object does not have target repository set: {}", artifactDB);
            return;
        }
        RepositoryType repositoryType = targetRepository.getRepositoryType();
        if (repositoryType.equals(RepositoryType.MAVEN) || repositoryType.equals(RepositoryType.NPM)) {
            if (artifactDB.getDeployPath() == null || artifactDB.getDeployPath().equals("")) {
                deployUrlSetter.accept("");
                publicUrlSetter.accept("");
            } else {
                try {
                    deployUrlSetter.accept(
                            UrlUtils.buildUrl(
                                    globalConfig.getIndyUrl(),
                                    targetRepository.getRepositoryPath(),
                                    artifactDB.getDeployPath()));
                    publicUrlSetter.accept(
                            UrlUtils.buildUrl(
                                    globalConfig.getExternalIndyUrl(),
                                    targetRepository.getRepositoryPath(),
                                    artifactDB.getDeployPath()));
                } catch (MalformedURLException e) {
                    logger.error("Cannot construct internal artifactDB URL.", e);
                    deployUrlSetter.accept(null);
                    publicUrlSetter.accept(null);
                }
            }
        } else {
            deployUrlSetter.accept(artifactDB.getOriginUrl());
            publicUrlSetter.accept(artifactDB.getOriginUrl());
        }
    }

    public void setConfig(Configuration config) {
        this.config = config;
    }

    public static class IDMapper {

        public static String toId(org.jboss.pnc.model.Artifact artifact) {
            return artifact.getId().toString();
        }

        public static org.jboss.pnc.model.Artifact toId(String artifactId) {
            return org.jboss.pnc.model.Artifact.Builder.newBuilder().id(Integer.valueOf(artifactId)).build();
        }
    }
}
