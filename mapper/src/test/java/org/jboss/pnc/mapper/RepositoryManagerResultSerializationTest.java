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

import org.jboss.pnc.dto.internal.RepositoryManagerResultRest;
import org.jboss.pnc.mapper.api.RepositoryManagerResultMapper;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.mapper.api.TargetRepositoryMapper;
import org.jboss.pnc.mapper.api.UserMapper;
import org.jboss.pnc.mock.spi.RepositoryManagerResultMock;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
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

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Test serialization of Repository manager rest
 */
@RunWith(MockitoJUnitRunner.class)
public class RepositoryManagerResultSerializationTest {

    private final Logger log = LoggerFactory.getLogger(RepositoryManagerResultSerializationTest.class);

    @Mock
    private Configuration configuration;

    @InjectMocks
    private TargetRepositoryMapper targetRepositoryMapper = Mockito.spy(new TargetRepositoryMapperImpl());

    @InjectMocks
    private BuildMapper buildMapper = Mockito.spy(new BuildMapperImpl());

    @InjectMocks
    private UserMapper userMapper = Mockito.spy(new UserMapperImpl());

    @InjectMocks
    private AbstractArtifactMapperImpl artifactMapper = spy(new AbstractArtifactMapperImpl());

    @InjectMocks
    private RefToReferenceMapper refMapper = spy(new RefToReferenceMapper());

    @InjectMocks
    private RepositoryManagerResultMapper repositoryManagerResultMapper = spy(new RepositoryManagerResultMapperImpl());

    @Before
    public void before() throws Exception {
        GlobalModuleGroup globalConfig = new GlobalModuleGroup();
        globalConfig.setIndyUrl("http://url.com");
        globalConfig.setExternalIndyUrl("http://url.com");
        when(configuration.getGlobalConfig()).thenReturn(globalConfig);
    }

    @Test
    public void serializeAndDeserializeRepositoryManagerResult() throws IOException {

        RepositoryManagerResult repoMngrResult = RepositoryManagerResultMock.mockResult();
        RepositoryManagerResultRest repoMngrResultRest = repositoryManagerResultMapper.toDTO(repoMngrResult);

        String repoMngrResultJson = JsonOutputConverterMapper.apply(repoMngrResultRest);
        log.debug("RepoManagerResultJson : {}", repoMngrResultJson);

        RepositoryManagerResultRest deserializedRepoManResultRest = JsonOutputConverterMapper
                .readValue(repoMngrResultJson, RepositoryManagerResultRest.class);
        RepositoryManagerResult deserializedRepoMngrResult = repositoryManagerResultMapper
                .toEntity(deserializedRepoManResultRest);
        String message = "Deserialized object does not match the original.";

        Assert.assertEquals(
                message,
                repoMngrResult.getBuildContentId(),
                deserializedRepoMngrResult.getBuildContentId());
        Assert.assertEquals(
                message,
                repoMngrResult.getBuiltArtifacts().get(0),
                deserializedRepoMngrResult.getBuiltArtifacts().get(0));
        Assert.assertEquals(
                message,
                repoMngrResult.getBuiltArtifacts().get(0).getSha256(),
                deserializedRepoMngrResult.getBuiltArtifacts().get(0).getSha256());
        Assert.assertEquals(
                message,
                repoMngrResult.getBuiltArtifacts().get(0).isBuilt(),
                deserializedRepoMngrResult.getBuiltArtifacts().get(0).isBuilt());
        Assert.assertEquals(
                message,
                repoMngrResult.getDependencies().get(0),
                deserializedRepoMngrResult.getDependencies().get(0));
        Assert.assertEquals(
                message,
                repoMngrResult.getDependencies().get(0).getDeployPath(),
                deserializedRepoMngrResult.getDependencies().get(0).getDeployPath());
        Assert.assertEquals(
                message,
                repoMngrResult.getDependencies().get(0).isImported(),
                deserializedRepoMngrResult.getDependencies().get(0).isImported());
    }
}
