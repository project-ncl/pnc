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
package org.jboss.pnc.mapper.abstracts;

import javax.inject.Inject;
import org.jboss.pnc.mapper.CollectionMerger;
import org.jboss.pnc.mapper.MapSetMapper;
import org.jboss.pnc.mapper.RefToReferenceMapper;
import org.jboss.pnc.mapper.api.GroupConfigurationMapper;
import org.jboss.pnc.mapper.api.MapperCentralConfig;
import org.jboss.pnc.mapper.api.ProductVersionMapper;
import org.mapstruct.Mapper;

/**
 *
 * @author jbrazdil
 */
@Mapper(
        config = MapperCentralConfig.class,
        implementationName = "GroupConfigurationMapperImpl",
        uses = { RefToReferenceMapper.class, ProductVersionMapper.class, MapSetMapper.class })
public abstract class AbstractGroupConfigurationMapper implements GroupConfigurationMapper {

    @Inject
    protected CollectionMerger cm;

}
