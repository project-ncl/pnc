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

import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.bpm.model.BuildExecutionConfigurationRest;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.enums.BuildType;
import org.jboss.pnc.enums.SystemImageType;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildExecutionConfigurationTest {
    private final Logger log = LoggerFactory.getLogger(BuildExecutionConfigurationTest.class);

    @Test
    public void serializeAndDeserializeBuildResult() throws IOException, BuildDriverException {

        BuildExecutionConfiguration buildExecutionConfiguration = BuildExecutionConfiguration.build(
                "1",
                "condent-id",
                "1",
                "mvn clean install",
                "configuration name",
                "12",
                "https://pathToRepo.git",
                "f18de64523d5054395d82e24d4e28473a05a3880",
                "1.0.0.Final-redhat-00001",
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
                AlignmentPreference.PREFER_PERSISTENT);
        BuildExecutionConfigurationRest buildExecutionConfigurationREST = new BuildExecutionConfigurationRest(
                buildExecutionConfiguration);

        String buildExecutionConfigurationJson = buildExecutionConfigurationREST.toString();
        log.debug("Json : {}", buildExecutionConfigurationJson);

        BuildExecutionConfigurationRest buildExecutionConfigurationRestFromJson = JsonOutputConverterMapper
                .readValue(buildExecutionConfigurationJson, BuildExecutionConfigurationRest.class);
        BuildExecutionConfiguration buildExecutionConfigurationFromJson = buildExecutionConfigurationRestFromJson
                .toBuildExecutionConfiguration();
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
