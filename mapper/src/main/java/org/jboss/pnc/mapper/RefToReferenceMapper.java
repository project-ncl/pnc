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
package org.jboss.pnc.mapper;

import java.io.Serializable;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import org.jboss.pnc.dto.ArtifactRef;
import org.jboss.pnc.dto.BuildConfigurationRef;
import org.jboss.pnc.dto.BuildPushResultRef;
import org.jboss.pnc.dto.BuildRef;
import org.jboss.pnc.dto.DTOEntity;
import org.jboss.pnc.dto.Environment;
import org.jboss.pnc.dto.GroupBuildRef;
import org.jboss.pnc.dto.GroupConfigurationRef;
import org.jboss.pnc.dto.ProductMilestoneCloseResultRef;
import org.jboss.pnc.dto.ProductMilestoneRef;
import org.jboss.pnc.dto.ProductRef;
import org.jboss.pnc.dto.ProductReleaseRef;
import org.jboss.pnc.dto.ProductVersionRef;
import org.jboss.pnc.dto.ProjectRef;
import org.jboss.pnc.dto.SCMRepository;
import org.jboss.pnc.mapper.api.ArtifactMapper;
import org.jboss.pnc.mapper.api.BuildConfigurationMapper;
import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.mapper.api.BuildPushResultMapper;
import org.jboss.pnc.mapper.api.EnvironmentMapper;
import org.jboss.pnc.mapper.api.GroupBuildMapper;
import org.jboss.pnc.mapper.api.GroupConfigurationMapper;
import org.jboss.pnc.mapper.api.IdEntity;
import org.jboss.pnc.mapper.api.IdMapper;
import org.jboss.pnc.mapper.api.ProductMapper;
import org.jboss.pnc.mapper.api.ProductMilestoneCloseResultMapper;
import org.jboss.pnc.mapper.api.ProductMilestoneMapper;
import org.jboss.pnc.mapper.api.ProductReleaseMapper;
import org.jboss.pnc.mapper.api.ProductVersionMapper;
import org.jboss.pnc.mapper.api.ProjectMapper;
import org.jboss.pnc.mapper.api.SCMRepositoryMapper;
import org.jboss.pnc.mapper.api.TargetRepositoryMapper;
import org.jboss.pnc.mapper.api.UserMapper;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecordPushResult;
import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductMilestoneRelease;
import org.jboss.pnc.model.ProductRelease;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.RepositoryConfiguration;
import org.jboss.pnc.model.TargetRepository;
import org.jboss.pnc.model.User;

/**
 *
 * @author jbrazdil
 */
@ApplicationScoped
@Transactional
public class RefToReferenceMapper {

    @Inject
    private EntityManager em;

    @Inject
    private ArtifactMapper artifactMapper;
    @Inject
    private BuildConfigurationMapper buildConfigurationMapper;
    @Inject
    private BuildMapper buildMapper;
    @Inject
    private BuildPushResultMapper buildPushResultMapper;
    @Inject
    private EnvironmentMapper environmentMapper;
    @Inject
    private UserMapper userMapper;
    @Inject
    private SCMRepositoryMapper scmRepositoryMapper;
    @Inject
    private ProjectMapper projectMapper;
    @Inject
    private ProductVersionMapper productVersionMapper;
    @Inject
    private ProductReleaseMapper productReleaseMapper;
    @Inject
    private GroupBuildMapper groupBuildMapper;
    @Inject
    private GroupConfigurationMapper groupConfigurationMapper;
    @Inject
    private ProductMilestoneCloseResultMapper productMilestoneCloseResultMapper;
    @Inject
    private ProductMapper productMapper;
    @Inject
    private ProductMilestoneMapper productMilestoneMapper;
    @Inject
    private TargetRepositoryMapper targetRepositoryMapper;

    public <ID extends Serializable, DB extends GenericEntity<ID>, REF extends DTOEntity> DB map(
            REF dtoEntity,
            IdMapper<ID, String> idMapper,
            Class<DB> type) {
        if (dtoEntity == null) {
            return null;
        }
        return em.getReference(type, idMapper.toEntity(dtoEntity.getId()));
    }

    public Artifact toEntityReference(ArtifactRef dtoEntity) {
        return map(dtoEntity, artifactMapper.getIdMapper(), Artifact.class);
    }

    public BuildConfiguration toEntityReference(BuildConfigurationRef dtoEntity) {
        return map(dtoEntity, buildConfigurationMapper.getIdMapper(), BuildConfiguration.class);
    }

    @IdEntity
    public BuildRecord toEntityReference(BuildRef dtoEntity) {
        return map(dtoEntity, buildMapper.getIdMapper(), BuildRecord.class);
    }

    public BuildRecordPushResult toEntityReference(BuildPushResultRef dtoEntity) {
        return map(dtoEntity, buildPushResultMapper.getIdMapper(), BuildRecordPushResult.class);
    }

    @IdEntity
    public BuildEnvironment toEntityReference(Environment dtoEntity) {
        return map(dtoEntity, environmentMapper.getIdMapper(), BuildEnvironment.class);
    }

    public BuildConfigSetRecord toEntityReference(GroupBuildRef dtoEntity) {
        return map(dtoEntity, groupBuildMapper.getIdMapper(), BuildConfigSetRecord.class);
    }

    public BuildConfigurationSet toEntityReference(GroupConfigurationRef dtoEntity) {
        return map(dtoEntity, groupConfigurationMapper.getIdMapper(), BuildConfigurationSet.class);
    }

    public Product toEntityReference(ProductRef dtoEntity) {
        return map(dtoEntity, productMapper.getIdMapper(), Product.class);
    }

    public ProductMilestone toEntityReference(ProductMilestoneRef dtoEntity) {
        return map(dtoEntity, productMilestoneMapper.getIdMapper(), ProductMilestone.class);
    }

    public ProductMilestoneRelease toEntityReference(ProductMilestoneCloseResultRef dtoEntity) {
        return map(dtoEntity, productMilestoneCloseResultMapper.getIdMapper(), ProductMilestoneRelease.class);
    }

    public ProductRelease toEntityReference(ProductReleaseRef dtoEntity) {
        return map(dtoEntity, productReleaseMapper.getIdMapper(), ProductRelease.class);
    }

    public ProductVersion toEntityReference(ProductVersionRef dtoEntity) {
        return map(dtoEntity, productVersionMapper.getIdMapper(), ProductVersion.class);
    }

    public Project toEntityReference(ProjectRef dtoEntity) {
        return map(dtoEntity, projectMapper.getIdMapper(), Project.class);
    }

    @IdEntity
    public RepositoryConfiguration toEntityReference(SCMRepository dtoEntity) {
        return map(dtoEntity, scmRepositoryMapper.getIdMapper(), RepositoryConfiguration.class);
    }

    @IdEntity
    public TargetRepository toEntityReference(org.jboss.pnc.dto.TargetRepository dtoEntity) {
        return map(dtoEntity, targetRepositoryMapper.getIdMapper(), TargetRepository.class);
    }

    @IdEntity
    public User toEntityReference(org.jboss.pnc.dto.User dtoEntity) {
        return map(dtoEntity, userMapper.getIdMapper(), User.class);
    }

}
