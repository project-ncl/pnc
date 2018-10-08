/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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

package org.jboss.pnc.restmodel.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.pnc.mock.repositorymanager.RepositoryManagerResultMock;
import org.jboss.pnc.rest.restmodel.RepositoryManagerResultRest;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Test serialization of Repository manager rest
 */
public class RepositoryManagerResultSerializationTest {

    private final Logger log = LoggerFactory.getLogger(RepositoryManagerResultSerializationTest.class);

    @Test
    public void serializeAndDeserializeRepositoryManagerResult() throws IOException, BuildDriverException {

        ObjectMapper mapper = new ObjectMapper();

        RepositoryManagerResult repoMngrResult = RepositoryManagerResultMock.mockResult();
        RepositoryManagerResultRest repoMngrResultRest = new RepositoryManagerResultRest(repoMngrResult);

        String repoMngrResultJson = JsonOutputConverterMapper.apply(repoMngrResultRest);
        log.debug("RepoManagerResultJson : {}", repoMngrResultJson);

        RepositoryManagerResultRest deserializedRepoManResultRest = mapper.readValue(repoMngrResultJson, RepositoryManagerResultRest.class);
        RepositoryManagerResult deserializedRepoMngrResult = deserializedRepoManResultRest.toRepositoryManagerResult();
        String message = "Deserialized object does not match the original.";

        Assert.assertEquals(message, repoMngrResult.getBuildContentId(), deserializedRepoMngrResult.getBuildContentId());
        Assert.assertEquals(message, repoMngrResult.getBuiltArtifacts().get(0), deserializedRepoMngrResult.getBuiltArtifacts().get(0));
        Assert.assertEquals(message, repoMngrResult.getBuiltArtifacts().get(0).getSha256(), deserializedRepoMngrResult.getBuiltArtifacts().get(0).getSha256());
        Assert.assertEquals(message, repoMngrResult.getBuiltArtifacts().get(0).isBuilt(), deserializedRepoMngrResult.getBuiltArtifacts().get(0).isBuilt());
        Assert.assertEquals(message, repoMngrResult.getDependencies().get(0), deserializedRepoMngrResult.getDependencies().get(0));
        Assert.assertEquals(message, repoMngrResult.getDependencies().get(0).getDeployPath(), deserializedRepoMngrResult.getDependencies().get(0).getDeployPath());
        Assert.assertEquals(message, repoMngrResult.getDependencies().get(0).isImported(), deserializedRepoMngrResult.getDependencies().get(0).isImported());
    }
}
