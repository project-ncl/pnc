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

import org.jboss.pnc.common.util.IoUtils;
import org.jboss.pnc.mock.spi.BuildResultMock;
import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.model.RepositoryType;
import org.jboss.pnc.rest.restmodel.BuildResultRest;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.builddriver.BuildDriverStatus;
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

        BuildResult buildResult = BuildResultMock.mock(BuildDriverStatus.SUCCESS);
        BuildResultRest buildResultRest = new BuildResultRest(buildResult);

        String buildResultJson = buildResultRest.toString();
        log.debug("BuildResultJson : {}", buildResultJson);

        BuildResultRest buildResultRestFromJson = new BuildResultRest(buildResultJson);
        BuildResult buildResultFromJson = buildResultRestFromJson.toBuildResult();
        String message = "Deserialized object does not match the original.";

        Assert.assertEquals(message, buildResult.hasFailed(), buildResultFromJson.hasFailed());
        Assert.assertEquals(message, buildResult.getException().get().getMessage(), buildResultFromJson.getException().get().getMessage());
        Assert.assertEquals(message, buildResult.getRepositoryManagerResult().get().getBuildContentId(), buildResultFromJson.getRepositoryManagerResult().get().getBuildContentId());
        Assert.assertEquals(message, buildResult.getRepositoryManagerResult().get().getBuiltArtifacts().get(0).getId(), buildResultFromJson.getRepositoryManagerResult().get().getBuiltArtifacts().get(0).getId());
        Assert.assertEquals(message, buildResult.getBuildDriverResult().get().getBuildLog(), buildResultFromJson.getBuildDriverResult().get().getBuildLog());
        Assert.assertEquals(message, buildResult.getBuildDriverResult().get().getBuildDriverStatus(), buildResultFromJson.getBuildDriverResult().get().getBuildDriverStatus());
        Assert.assertEquals(message, buildResult.getBuildExecutionConfiguration().get().getId(), buildResultFromJson.getBuildExecutionConfiguration().get().getId());
    }

    @Test
    public void deserializeLongResult() throws IOException, BuildDriverException {
        String serializedJson = IoUtils.readResource("BuildResult-long.json", this.getClass().getClassLoader());
        BuildResultRest buildResultRest = new BuildResultRest(serializedJson);

        Assert.assertEquals(34, buildResultRest.getBuildExecutionConfiguration().getId());
        Assert.assertEquals(BuildType.JAVA, buildResultRest.getBuildExecutionConfiguration().getBuildType());

        Assert.assertEquals(BuildDriverStatus.SUCCESS, buildResultRest.getBuildDriverResult().getBuildDriverStatus());
        Assert.assertTrue(buildResultRest.getBuildDriverResult().getBuildLog().contains("Welcome sir"));
        Assert.assertTrue(buildResultRest.getBuildDriverResult().getBuildLog().contains("Finished with status: COMPLETED"));

        Assert.assertTrue(buildResultRest.getRepositoryManagerResult().getBuiltArtifacts().size() > 0);
        Assert.assertEquals(RepositoryType.MAVEN, buildResultRest.getRepositoryManagerResult().getBuiltArtifacts().get(0).getRepoType());
        Assert.assertTrue(buildResultRest.getRepositoryManagerResult().getDependencies().size() > 0);
        Assert.assertEquals(RepositoryType.MAVEN, buildResultRest.getRepositoryManagerResult().getDependencies().get(0).getRepoType());
    }
}
