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

package org.jboss.pnc.rest.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.pnc.mock.spi.BuildResultMock;
import org.jboss.pnc.rest.notifications.websockets.JSonOutputConverter;
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

        JSonOutputConverter converter = new JSonOutputConverter();
        String buildResultJson = converter.apply(buildResultRest);
        log.debug("BuildResultJson : {}", buildResultJson);

        ObjectMapper mapper = new ObjectMapper();
        BuildResultRest buildResultRestFromJson = mapper.readValue(buildResultJson, BuildResultRest.class);
        BuildResult buildResultFromJson = buildResultRestFromJson.toBuildResult();
        Assert.assertTrue("Deserialized object does not match the original.", resultsEquals(buildResult, buildResultFromJson));
    }

    private boolean resultsEquals(BuildResult buildResult, BuildResult buildResultFromJson) throws BuildDriverException {
        if (buildResult.hasFailed() != buildResultFromJson.hasFailed()) {
            return false;
        }
        if (!buildResult.getException().get().getMessage().equals(buildResultFromJson.getException().get().getMessage())) {
            return false;
        }
        if (!buildResult.getRepositoryManagerResult().get().getBuildContentId().equals(buildResultFromJson.getRepositoryManagerResult().get().getBuildContentId())) {
            return false;
        }
        if (!buildResult.getRepositoryManagerResult().get().getBuiltArtifacts().get(0).getId().equals(buildResultFromJson.getRepositoryManagerResult().get().getBuiltArtifacts().get(0).getId())) {
            return false;
        }
        if (!buildResult.getBuildDriverResult().get().getBuildLog().equals(buildResultFromJson.getBuildDriverResult().get().getBuildLog())) {
            return false;
        }
        if (!buildResult.getBuildDriverResult().get().getBuildDriverStatus().equals(buildResultFromJson.getBuildDriverResult().get().getBuildDriverStatus())) {
            return false;
        }
        return true;
    }
}
