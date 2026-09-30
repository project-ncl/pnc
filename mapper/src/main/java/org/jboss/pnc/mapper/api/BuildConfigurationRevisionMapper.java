/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mapper.api;

import org.jboss.pnc.dto.BuildConfigurationRevision;
import org.jboss.pnc.dto.BuildConfigurationRevisionRef;
import org.jboss.pnc.dto.ProjectRef;
import org.jboss.pnc.mapper.RefToReferenceMapper;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.IdRev;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author <a href="mailto:jmichalo@redhat.com">Jan Michalov</a>
 */
@Mapper(
        config = MapperCentralConfig.class,
        uses = {
                RefToReferenceMapper.class,
                ProjectMapper.class,
                EnvironmentMapper.class,
                SCMRepositoryMapper.class,
                UserMapper.class },
        imports = IdRev.class)
public interface BuildConfigurationRevisionMapper {

    @Mapping(target = "id", expression = "java( dbEntity.getId().toString() )")
    @Mapping(target = "scmRepository", source = "repositoryConfiguration", qualifiedBy = Reference.class)
    @Mapping(target = "environment", source = "buildEnvironment", qualifiedBy = Reference.class)
    @Mapping(target = "project", resultType = ProjectRef.class)
    @Mapping(target = "modificationTime", source = "lastModificationTime")
    @Mapping(
            target = "parameters",
            expression = "java( BuildConfigurationParametersUtils.withDefaults(dbEntity.getGenericParameters()) )")
    @Mapping(target = "creationUser", qualifiedBy = Reference.class)
    @Mapping(target = "modificationUser", source = "lastModificationUser", qualifiedBy = Reference.class)
    @BeanMapping(ignoreUnmappedSourceProperties = { "idRev", "buildConfiguration", "genericParameters" })
    BuildConfigurationRevision toDTO(BuildConfigurationAudited dbEntity);

    @Mapping(target = "repositoryConfiguration", source = "scmRepository", qualifiedBy = IdEntity.class)
    @Mapping(target = "buildEnvironment", source = "environment", qualifiedBy = IdEntity.class)
    @Mapping(
            target = "idRev",
            expression = "java( new IdRev( Integer.valueOf(dtoEntity.getId()), dtoEntity.getRev() ) )")
    @Mapping(target = "buildConfiguration", ignore = true)
    @Mapping(target = "lastModificationTime", source = "modificationTime")
    @Mapping(target = "genericParameters", source = "parameters")
    @Mapping(target = "creationUser", qualifiedBy = IdEntity.class)
    @Mapping(target = "lastModificationUser", source = "modificationUser", qualifiedBy = IdEntity.class)
    BuildConfigurationAudited toEntity(BuildConfigurationRevision dtoEntity);

    @Mapping(target = "id", expression = "java( dbEntity.getId().toString() )")
    @Mapping(target = "modificationTime", source = "lastModificationTime")
    @BeanMapping(
            ignoreUnmappedSourceProperties = {
                    "idRev",
                    "buildConfiguration",
                    "repositoryConfiguration",
                    "buildEnvironment",
                    "project",
                    "genericParameters",
                    "creationUser",
                    "lastModificationUser" })
    BuildConfigurationRevisionRef toRef(BuildConfigurationAudited dbEntity);
}
