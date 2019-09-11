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
package org.jboss.pnc.mapper;

import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfigurationRevision;
import org.jboss.pnc.mapper.api.BuildConfigurationRevisionMapper;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.mapstruct.BeforeMapping;
import org.mapstruct.MappingTarget;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Workaround for NCL-4889.
 * @author jbrazdil
 */
@ApplicationScoped
public class BuildBCRevisionFetcher {

    @Inject
    private BuildConfigurationRevisionMapper bcRevisionMapper;

    @Inject
    private BuildConfigurationAuditedRepository bcAuditedRepository;

    @BeforeMapping
    public void mockBrewAttributes(BuildRecord build, @MappingTarget Build.Builder dtoBuilder) {
        Integer id = build.getBuildConfigurationId();
        Integer revision = build.getBuildConfigurationRev();

        BuildConfigurationAudited buildConfigurationAudited = bcAuditedRepository
                .queryById(new IdRev(id, revision));

        BuildConfigurationRevision bcRevision = bcRevisionMapper.toDTO(buildConfigurationAudited);
        dtoBuilder.buildConfigRevision(bcRevision);
    }
}
