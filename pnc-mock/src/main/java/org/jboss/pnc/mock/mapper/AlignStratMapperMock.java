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

import org.jboss.pnc.dto.AlignmentStrategy;
import org.jboss.pnc.mapper.api.AlignStratMapper;
import org.jboss.pnc.model.AlignStrategy;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

@Alternative
@ApplicationScoped
public class AlignStratMapperMock implements AlignStratMapper {

    @Override
    public AlignStrategy toModel(AlignmentStrategy strat) {
        return AlignStrategy.builder().ranks(strat.getRanks()).denyList(strat.getDenyList()).build();
    }

    @Override
    public AlignmentStrategy toDto(AlignStrategy strat, String dependencyOverride) {
        return AlignmentStrategy.builder()
                .denyList(strat.getDenyList())
                .allowList(strat.getAllowList())
                .ranks(strat.getRanks())
                .dependencyOverride(dependencyOverride)
                .build();
    }

    @Override
    public void updateEntity(AlignmentStrategy dto, AlignStrategy model) {
        model.setRanks(dto.getRanks());
        model.setAllowList(dto.getAllowList());
        model.setDenyList(dto.getDenyList());
    }
}
