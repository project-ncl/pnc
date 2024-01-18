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
import org.mapstruct.Named;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Workaround for NCL-4889 and NCL-5257. This class will fetch the audited Build Config from DB if it is missing from
 * the transient filed in BuildRecord entity and will map it to appropriate fields in the Build DTO.
 *
 * @author jbrazdil
 */
@ApplicationScoped
public class BuildBCRevisionFetcher {

    @Inject
    private BuildConfigurationRevisionMapper bcRevisionMapper;

    @Inject
    private ProjectMapper projectMapper;

    @Inject
    private EnvironmentMapper environmentMapper;

    @Inject
    private SCMRepositoryMapper scmRepositoryMapper;

    @Inject
    private BuildConfigurationAuditedRepository bcAuditedRepository;

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
