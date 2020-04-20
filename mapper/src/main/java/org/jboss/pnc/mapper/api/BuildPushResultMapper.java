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
package org.jboss.pnc.mapper.api;

import org.jboss.pnc.dto.BuildPushResult;
import org.jboss.pnc.dto.BuildPushResultRef;
import org.jboss.pnc.dto.ProductMilestoneCloseResultRef;
import org.jboss.pnc.mapper.UUIDMapper;
import org.jboss.pnc.model.BuildRecordPushResult;
import org.jboss.pnc.model.ProductMilestoneRelease;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

/**
 * @author <a href="mailto:jmichalo@redhat.com">Jan Michalov</a>
 */
@Mapper(
        config = MapperCentralConfig.class,
        uses = { UUIDMapper.class, BuildMapper.IDMapper.class, ProductMilestoneCloseResultMapper.class })
public interface BuildPushResultMapper
        extends EntityMapper<UUID, BuildRecordPushResult, BuildPushResult, BuildPushResultRef> {

    @Override
    default BuildRecordPushResult toIDEntity(BuildPushResultRef dtoEntity) {
        if (dtoEntity == null) {
            return null;
        }
        BuildRecordPushResult buildRecordPushResult = new BuildRecordPushResult();
        buildRecordPushResult.setId(UUID.fromString(dtoEntity.getId()));
        return buildRecordPushResult;
    }

    @Mapping(target = "buildId", source = "buildRecord")
    @Mapping(
            target = "productMilestoneCloseResult",
            source = "productMilestoneRelease",
            resultType = ProductMilestoneCloseResultRef.class)
    @Mapping(target = "logContext", expression = "java( logContext(db) )")
    @Mapping(target = "message", ignore = true)
    @BeanMapping(ignoreUnmappedSourceProperties = { "tagPrefix", "artifactImportErrors", "log" })
    BuildPushResult toDTO(BuildRecordPushResult db);

    @Mapping(target = "buildId", source = "buildRecord")
    @BeanMapping(
            ignoreUnmappedSourceProperties = { "tagPrefix", "artifactImportErrors", "log", "productMilestoneRelease" })
    @Mapping(target = "logContext", expression = "java( logContext(db) )")
    @Mapping(target = "message", ignore = true)
    BuildPushResultRef toRef(BuildRecordPushResult db);

    @Mapping(target = "buildRecord", source = "buildId")
    @Mapping(target = "tagPrefix", ignore = true)
    @Mapping(target = "log", ignore = true)
    @Mapping(target = "productMilestoneRelease", source = "dto.productMilestoneCloseResult")
    @BeanMapping(ignoreUnmappedSourceProperties = { "logContext", "message" })
    BuildRecordPushResult toEntity(BuildPushResult dto);

    // maybe default, because private static may not work in Java 8?
    default String logContext(BuildRecordPushResult db) {
        ProductMilestoneRelease productMilestoneRelease = db.getProductMilestoneRelease();
        if (productMilestoneRelease != null) { // is part of milestone release
            return productMilestoneRelease.getId().toString();
        } else {
            return db.getId().toString();
        }
    }
}
