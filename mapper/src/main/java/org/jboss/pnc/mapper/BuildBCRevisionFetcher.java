/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mapper;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfigurationRevisionRef;
import org.jboss.pnc.dto.Environment;
import org.jboss.pnc.dto.ProjectRef;
import org.jboss.pnc.dto.SCMRepository;
import org.jboss.pnc.mapper.api.BuildConfigurationRevisionMapper;
import org.jboss.pnc.mapper.api.EnvironmentMapper;
import org.jboss.pnc.mapper.api.ProjectMapper;
import org.jboss.pnc.mapper.api.SCMRepositoryMapper;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.mapstruct.BeforeMapping;
import org.mapstruct.MappingTarget;

/**
 * Workaround for NCL-4889 and NCL-5257. This class will fetch the audited Build Config from DB if it is missing from
 * the transient filed in BuildRecord entity and will map it to appropriate fields in the Build DTO.
 *
 * @author jbrazdil
 */
@ApplicationScoped
public class BuildBCRevisionFetcher {

    private BuildConfigurationRevisionMapper bcRevisionMapper;

    private ProjectMapper projectMapper;

    private EnvironmentMapper environmentMapper;

    private SCMRepositoryMapper scmRepositoryMapper;

    private BuildConfigurationAuditedRepository bcAuditedRepository;

    // CDI
    public BuildBCRevisionFetcher() {
    }

    @Inject
    public BuildBCRevisionFetcher(
            BuildConfigurationRevisionMapper bcRevisionMapper,
            ProjectMapper projectMapper,
            EnvironmentMapper environmentMapper,
            SCMRepositoryMapper scmRepositoryMapper,
            BuildConfigurationAuditedRepository bcAuditedRepository) {
        this.bcRevisionMapper = bcRevisionMapper;
        this.projectMapper = projectMapper;
        this.environmentMapper = environmentMapper;
        this.scmRepositoryMapper = scmRepositoryMapper;
        this.bcAuditedRepository = bcAuditedRepository;
    }

    @BeforeMapping
    @BuildHelpers
    public void mapFromAuditedBuildConfig(BuildRecord build, @MappingTarget Build.Builder dtoBuilder) {
        Integer id = build.getBuildConfigurationId();
        Integer revision = build.getBuildConfigurationRev();

        // If somebody before us already set the BCA we don't need to query it from DB again
        BuildConfigurationAudited bca = build.getBuildConfigurationAudited();
        if (bca == null) {
            bca = bcAuditedRepository.queryById(new IdRev(id, revision));
        }

        BuildConfigurationRevisionRef bcRevision = bcRevisionMapper.toRef(bca);
        ProjectRef project = projectMapper.toRef(bca.getProject());
        Environment environment = environmentMapper.toRef(bca.getBuildEnvironment());
        SCMRepository scmRepository = scmRepositoryMapper.toRef(bca.getRepositoryConfiguration());

        dtoBuilder.buildConfigRevision(bcRevision);
        dtoBuilder.project(project);
        dtoBuilder.environment(environment);
        dtoBuilder.scmRepository(scmRepository);
    }
}
