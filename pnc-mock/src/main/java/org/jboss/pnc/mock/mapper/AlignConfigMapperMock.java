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

package org.jboss.pnc.mock.mapper;

import org.jboss.pnc.dto.AlignmentConfig;
import org.jboss.pnc.mapper.api.AlignConfigMapper;
import org.jboss.pnc.model.AlignConfig;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

@Alternative
@ApplicationScoped
public class AlignConfigMapperMock implements AlignConfigMapper {

    @Override
    public AlignConfig toModel(AlignmentConfig config) {
        return AlignConfig.builder().ranks(config.getRanks()).denyList(config.getDenyList()).build();
    }

    @Override
    public AlignmentConfig toDto(AlignConfig config, String dependencyOverride) {
        return AlignmentConfig.builder()
                .denyList(config.getDenyList())
                .allowList(config.getAllowList())
                .ranks(config.getRanks())
                .dependencyOverride(dependencyOverride)
                .build();
    }

    @Override
    public void updateEntity(AlignmentConfig dto, AlignConfig model) {
        model.setRanks(dto.getRanks());
        model.setAllowList(dto.getAllowList());
        model.setDenyList(dto.getDenyList());
    }
}
