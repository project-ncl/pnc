/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.mock.spi.BuildResultMock;
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.rest.restmodel.bpm.BuildResultRest;
import org.jboss.pnc.rest.utils.JsonOutputConverterMapper;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildResultSerializationTest {

    private final Logger log = LoggerFactory.getLogger(BuildResultSerializationTest.class);

    @Test
    public void serializeAndDeserializeBuildResult() throws IOException, BuildDriverException {

        BuildResult buildResult = BuildResultMock.mock(BuildStatus.SUCCESS);
        BuildResultRest buildResultRest = new BuildResultRest(buildResult);

        String buildResultJson = buildResultRest.toString();
        log.debug("BuildResultJson : {}", buildResultJson);

        BuildResultRest buildResultRestFromJson = JsonOutputConverterMapper.readValue(buildResultJson, BuildResultRest.class);

        BuildResult buildResultFromJson = buildResultRestFromJson.toBuildResult();
        String message = "Deserialized object does not match the original.";

        Assert.assertEquals(message, buildResult.hasFailed(), buildResultFromJson.hasFailed());
        Assert.assertEquals(message, buildResult.getCompletionStatus(), buildResultFromJson.getCompletionStatus());
        Assert.assertEquals(message, buildResult.getProcessException().get().getMessage(), buildResultFromJson.getProcessException().get().getMessage());

        Assert.assertEquals(message, buildResult.getBuildExecutionConfiguration().get().getId(), buildResultFromJson.getBuildExecutionConfiguration().get().getId());

        Assert.assertEquals(message, buildResult.getRepositoryManagerResult().get().getBuildContentId(), buildResultFromJson.getRepositoryManagerResult().get().getBuildContentId());
        Assert.assertEquals(message, buildResult.getRepositoryManagerResult().get().getBuiltArtifacts().get(0).getId(), buildResultFromJson.getRepositoryManagerResult().get().getBuiltArtifacts().get(0).getId());

        Assert.assertEquals(message, buildResult.getBuildDriverResult().get().getBuildLog(), buildResultFromJson.getBuildDriverResult().get().getBuildLog());
        Assert.assertEquals(message, buildResult.getBuildDriverResult().get().getBuildStatus(), buildResultFromJson.getBuildDriverResult().get().getBuildStatus());

        Assert.assertEquals(message, buildResult.getRepourResult().get().getCompletionStatus(), buildResultFromJson.getRepourResult().get().getCompletionStatus());
        Assert.assertEquals(message, buildResult.getRepourResult().get().getExecutionRootName(), buildResultFromJson.getRepourResult().get().getExecutionRootName());

        Assert.assertEquals(message, buildResult.getEnvironmentDriverResult().get().getCompletionStatus(), buildResultFromJson.getEnvironmentDriverResult().get().getCompletionStatus());
        Assert.assertEquals(message, buildResult.getEnvironmentDriverResult().get().getSshCredentials().get().getCommand(), buildResultFromJson.getEnvironmentDriverResult().get().getSshCredentials().get().getCommand());

    }
}
