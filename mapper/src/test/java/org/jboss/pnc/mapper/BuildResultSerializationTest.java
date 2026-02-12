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

import org.jboss.pnc.mapper.api.BuildDriverResultMapper;
import org.jboss.pnc.mapper.api.BuildExecutionConfigurationMapper;
import org.jboss.pnc.mapper.api.BuildResultMapper;
import org.jboss.pnc.mapper.api.EnvironmentDriverResultMapper;
import org.jboss.pnc.mapper.api.RepositoryManagerResultMapper;
import org.jboss.pnc.dto.internal.BuildResultRest;
import org.jboss.pnc.mapper.api.RepourResultMapper;
import org.jboss.pnc.mapper.api.SshCredentialsMapper;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.mapper.api.ArtifactMapper;
import org.jboss.pnc.mapper.api.TargetRepositoryMapper;
import org.jboss.pnc.mapper.api.UserMapper;
import org.jboss.pnc.mock.spi.BuildResultMock;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import org.jboss.pnc.mapper.api.BuildMapper;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class BuildResultSerializationTest {

    private final Logger log = LoggerFactory.getLogger(BuildResultSerializationTest.class);

    @Mock
    private Configuration configuration;

    @InjectMocks
    protected TargetRepositoryMapper targetRepositoryMapper = Mockito.spy(new TargetRepositoryMapperImpl());

    @InjectMocks
    protected BuildMapper buildMapper = Mockito.spy(new BuildMapperImpl());

    @InjectMocks
    protected UserMapper userMapper = Mockito.spy(new UserMapperImpl());

    @InjectMocks
    protected ArtifactMapper artifactMapper = Mockito.spy(new AbstractArtifactMapperImpl());

    @InjectMocks
    protected RefToReferenceMapper refMapper = spy(new RefToReferenceMapper());

    @InjectMocks
    protected SshCredentialsMapper sshCredentialsMapper = spy(new SshCredentialsMapperImpl());

    @InjectMocks
    private ProcessExceptionMapper processExceptionMapper = spy(new ProcessExceptionMapper());

    @InjectMocks
    private BuildExecutionConfigurationMapper buildExecutionConfigurationMapper = spy(
            new BuildExecutionConfigurationMapperImpl());

    @InjectMocks
    private EnvironmentDriverResultMapper environmentDriverResultMapper = spy(new EnvironmentDriverResultMapperImpl());

    @InjectMocks
    private BuildDriverResultMapper buildDriverResultMapper = spy(new BuildDriverResultMapperImpl());

    @InjectMocks
    private RepourResultMapper repourResultMapper = spy(new RepourResultMapperImpl());

    @InjectMocks
    private RepositoryManagerResultMapper irepositoryManagerResultMapper = spy(new RepositoryManagerResultMapperImpl());

    @InjectMocks
    private BuildResultMapper buildResultRestMapper = spy(new BuildResultMapperImpl());

    @Before
    public void before() throws Exception {
        GlobalModuleGroup globalConfig = new GlobalModuleGroup();
        globalConfig.setIndyUrl("http://url.com");
        globalConfig.setExternalIndyUrl("http://url.com");
        when(configuration.getGlobalConfig()).thenReturn(globalConfig);
    }

    @Test
    public void serializeAndDeserializeBuildResult() throws IOException, BuildDriverException {

        BuildResult buildResult = BuildResultMock.mock(BuildStatus.SUCCESS);
        BuildResultRest buildResultRest = buildResultRestMapper.toDTO(buildResult);

        String buildResultJson = JsonOutputConverterMapper.apply(buildResultRest);
        log.debug("BuildResultJson : {}", buildResultJson);

        BuildResultRest buildResultRestFromJson = JsonOutputConverterMapper
                .readValue(buildResultJson, BuildResultRest.class);

        BuildResult buildResultFromJson = buildResultRestMapper.toEntity(buildResultRestFromJson);
        String message = "Deserialized object does not match the original.";

        Assert.assertEquals(message, buildResult.hasFailed(), buildResultFromJson.hasFailed());
        Assert.assertEquals(message, buildResult.getCompletionStatus(), buildResultFromJson.getCompletionStatus());
        Assert.assertEquals(
                message,
                buildResult.getProcessException().get().getMessage(),
                buildResultFromJson.getProcessException().get().getMessage());

        Assert.assertEquals(
                message,
                buildResult.getBuildExecutionConfiguration().get().getId(),
                buildResultFromJson.getBuildExecutionConfiguration().get().getId());

        Assert.assertEquals(
                message,
                buildResult.getRepositoryManagerResult().get().getBuildContentId(),
                buildResultFromJson.getRepositoryManagerResult().get().getBuildContentId());
        Assert.assertEquals(
                message,
                buildResult.getRepositoryManagerResult().get().getBuiltArtifacts().get(0).getId(),
                buildResultFromJson.getRepositoryManagerResult().get().getBuiltArtifacts().get(0).getId());

        Assert.assertEquals(
                message,
                buildResult.getBuildDriverResult().get().getBuildStatus(),
                buildResultFromJson.getBuildDriverResult().get().getBuildStatus());

        Assert.assertEquals(
                message,
                buildResult.getRepourResult().get().getCompletionStatus(),
                buildResultFromJson.getRepourResult().get().getCompletionStatus());
        Assert.assertEquals(
                message,
                buildResult.getRepourResult().get().getExecutionRootName(),
                buildResultFromJson.getRepourResult().get().getExecutionRootName());

        Assert.assertEquals(
                message,
                buildResult.getEnvironmentDriverResult().get().getCompletionStatus(),
                buildResultFromJson.getEnvironmentDriverResult().get().getCompletionStatus());
        Assert.assertEquals(
                message,
                buildResult.getEnvironmentDriverResult().get().getSshCredentials().get().getCommand(),
                buildResultFromJson.getEnvironmentDriverResult().get().getSshCredentials().get().getCommand());

    }
}
