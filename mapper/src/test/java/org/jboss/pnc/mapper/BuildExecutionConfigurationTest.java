/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mapper;

import static org.mockito.Mockito.spy;

import java.io.IOException;
import java.util.HashMap;

import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.api.enums.RebuildMode;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.dto.internal.BuildExecutionConfigurationRest;
import org.jboss.pnc.enums.BuildType;
import org.jboss.pnc.enums.SystemImageType;
import org.jboss.pnc.mapper.api.ArtifactMapper;
import org.jboss.pnc.mapper.api.BuildExecutionConfigurationMapper;
import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.mapper.api.TargetRepositoryMapper;
import org.jboss.pnc.mapper.api.UserMapper;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.executor.DefaultBuildExecutionConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class BuildExecutionConfigurationTest {
    private final Logger log = LoggerFactory.getLogger(BuildExecutionConfigurationTest.class);

    @Mock
    private Configuration configuration;

    @InjectMocks
    private TargetRepositoryMapper targetRepositoryMapper = Mockito.spy(new TargetRepositoryMapperImpl());

    @InjectMocks
    private BuildMapper buildMapper = Mockito.spy(new BuildMapperImpl());

    @InjectMocks
    private UserMapper userMapper = Mockito.spy(new UserMapperImpl());

    @InjectMocks
    private ArtifactMapper artifactMapper = Mockito.spy(new AbstractArtifactMapperImpl());

    @InjectMocks
    private RefToReferenceMapper refMapper = spy(new RefToReferenceMapper());

    @InjectMocks
    private BuildExecutionConfigurationMapper buildExecutionConfigurationMapper = spy(
            new BuildExecutionConfigurationMapperImpl());

    @Test
    public void serializeAndDeserializeBuildResult() throws IOException, BuildDriverException {

        BuildExecutionConfiguration buildExecutionConfiguration = new DefaultBuildExecutionConfiguration(
                "1",
                "condent-id",
                "1",
                "mvn clean install",
                "configuration name",
                "12",
                "https://pathToRepo.git",
                "f18de64523d5054395d82e24d4e28473a05a3880",
                "1.0.0.Final-redhat-00001",
                "e18de64523d5054395d82e24d4e28473a05a3880",
                false,
                "https://pathToOriginRepo.git",
                false,
                "abcd1234",
                "image.repo.url/repo",
                SystemImageType.DOCKER_IMAGE,
                BuildType.MVN,
                false,
                null,
                new HashMap<>(),
                false,
                null,
                false,
                "-DdependencySource=REST -DrepoRemovalBackup=repositories-backup.xml -DversionSuffixStrip= -DreportNonAligned=true",
                AlignmentPreference.PREFER_PERSISTENT,
                RebuildMode.IMPLICIT_DEPENDENCY_CHECK);
        BuildExecutionConfigurationRest buildExecutionConfigurationREST = buildExecutionConfigurationMapper
                .toDTO(buildExecutionConfiguration);

        String buildExecutionConfigurationJson = JsonOutputConverterMapper.apply(buildExecutionConfigurationREST);
        log.debug("Json : {}", buildExecutionConfigurationJson);

        BuildExecutionConfigurationRest buildExecutionConfigurationRestFromJson = JsonOutputConverterMapper
                .readValue(buildExecutionConfigurationJson, BuildExecutionConfigurationRest.class);
        BuildExecutionConfiguration buildExecutionConfigurationFromJson = buildExecutionConfigurationMapper
                .toEntity(buildExecutionConfigurationRestFromJson);
        String message = "Deserialized object does not match the original.";

        Assert.assertEquals(message, buildExecutionConfiguration.getId(), buildExecutionConfigurationFromJson.getId());
        Assert.assertEquals(
                message,
                buildExecutionConfiguration.getBuildScript(),
                buildExecutionConfigurationFromJson.getBuildScript());
        Assert.assertEquals(
                message,
                buildExecutionConfiguration.getName(),
                buildExecutionConfigurationFromJson.getName());
        Assert.assertEquals(
                message,
                buildExecutionConfiguration.getScmRepoURL(),
                buildExecutionConfigurationFromJson.getScmRepoURL());
        Assert.assertEquals(
                message,
                buildExecutionConfiguration.getScmRevision(),
                buildExecutionConfigurationFromJson.getScmRevision());
        Assert.assertEquals(
                message,
                buildExecutionConfiguration.getScmTag(),
                buildExecutionConfigurationFromJson.getScmTag());
        Assert.assertEquals(
                message,
                buildExecutionConfiguration.getOriginRepoURL(),
                buildExecutionConfigurationFromJson.getOriginRepoURL());
        Assert.assertEquals(
                message,
                buildExecutionConfiguration.isPreBuildSyncEnabled(),
                buildExecutionConfigurationFromJson.isPreBuildSyncEnabled());
        Assert.assertEquals(
                message,
                buildExecutionConfiguration.getUserId(),
                buildExecutionConfigurationFromJson.getUserId());
    }

}
