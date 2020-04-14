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

package org.jboss.pnc.bpm.test;

import org.jboss.pnc.bpm.model.mapper.BuildResultMapper;
import org.jboss.pnc.bpm.model.mapper.RepositoryManagerResultMapper;
import org.jboss.pnc.bpm.model.BuildResultRest;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.moduleconfig.IndyRepoDriverModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.mapper.AbstractArtifactMapper;
import org.jboss.pnc.mapper.AbstractArtifactMapperImpl;
import org.jboss.pnc.mapper.api.TargetRepositoryMapper;
import org.jboss.pnc.mock.spi.BuildResultMock;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import org.jboss.pnc.mapper.api.BuildMapper;

import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class BuildResultSerializationTest {

    private final Logger log = LoggerFactory.getLogger(BuildResultSerializationTest.class);

    @Mock
    private Configuration configuration;

    @Spy
    private TargetRepositoryMapper targetRepositoryMapper;

    @Spy
    private BuildMapper buildMapper;

    @Spy
    private AbstractArtifactMapperImpl artifactMapper;

    @Spy
    private RepositoryManagerResultMapper repositoryManagerResultMapper;

    @Spy
    @InjectMocks
    private BuildResultMapper buildResultMapper;

    @Before
    public void before() throws Exception {
        IndyRepoDriverModuleConfig indyRepoDriverModuleConfig = new IndyRepoDriverModuleConfig("http://url.com");
        indyRepoDriverModuleConfig.setExternalRepositoryMvnPath("http://url.com");
        indyRepoDriverModuleConfig.setExternalRepositoryNpmPath("http://url.com");
        indyRepoDriverModuleConfig.setInternalRepositoryMvnPath("http://url.com");
        indyRepoDriverModuleConfig.setInternalRepositoryNpmPath("http://url.com");
        injectMethod(
                "artifactMapper",
                repositoryManagerResultMapper,
                artifactMapper,
                RepositoryManagerResultMapper.class);
        injectMethod("config", artifactMapper, configuration, AbstractArtifactMapper.class);
        injectMethod(
                "targetRepositoryMapper",
                artifactMapper,
                targetRepositoryMapper,
                AbstractArtifactMapperImpl.class);
        injectMethod("buildMapper", artifactMapper, buildMapper, AbstractArtifactMapperImpl.class);
        when(configuration.getModuleConfig(new PncConfigProvider<>(IndyRepoDriverModuleConfig.class)))
                .thenReturn(indyRepoDriverModuleConfig);
    }

    private void injectMethod(String fieldName, Object to, Object what, Class clazz)
            throws NoSuchFieldException, IllegalAccessException {
        Field f = clazz.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(to, what);
    }

    @Test
    public void serializeAndDeserializeBuildResult() throws IOException, BuildDriverException {

        BuildResult buildResult = BuildResultMock.mock(BuildStatus.SUCCESS);
        BuildResultRest buildResultRest = buildResultMapper.toDTO(buildResult);

        String buildResultJson = buildResultRest.toFullLogString();
        log.debug("BuildResultJson : {}", buildResultJson);

        BuildResultRest buildResultRestFromJson = JsonOutputConverterMapper
                .readValue(buildResultJson, BuildResultRest.class);

        BuildResult buildResultFromJson = buildResultMapper.toEntity(buildResultRestFromJson);
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
                buildResult.getBuildDriverResult().get().getBuildLog(),
                buildResultFromJson.getBuildDriverResult().get().getBuildLog());
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
