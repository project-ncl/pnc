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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.pnc.dto.internal.BuildExecutionConfigurationWithCallbackRest;
import org.junit.Test;

public class DeserializeTest {

    @Test
    public void shouldDeserializeBECWithCallback() throws JsonProcessingException {
        String json = "{\n" + "        \"id\": \"1394\",\n" + "        \"buildContentId\": \"build-1394\",\n"
                + "        \"user\": {\n" + "            \"id\": \"103\",\n" + "            \"email\": \"null\",\n"
                + "            \"firstName\": \"null\",\n" + "            \"lastName\": \"null\",\n"
                + "            \"username\": \"null\"\n" + "        },\n"
                + "        \"buildScript\": \"set +x\\n\\ntheFox=\\\"The quick brown fox jumps over the lazy dog\\\"\\nfor run in {1..20}; do echo \\\"${run} ${theFox}\\\"; sleep 1; done\\n\\n#sleep 600;\\necho \\\"No more sleep for you.\\\"\",\n"
                + "        \"name\": \"mlazar-empty\",\n"
                + "        \"scmRepoURL\": \"http:\\/\\/code.stage.engineering.redhat.com\\/matejonnet\\/test.git\",\n"
                + "        \"scmRevision\": \"4d396bc20620844c5fd54bfef54e439bdff7b80d\",\n"
                + "        \"originRepoURL\": null,\n" + "        \"preBuildSyncEnabled\": null,\n"
                + "        \"systemImageId\": \"newcastle\\/builder-rhel-7-j11-mvn3.6.0:latest\",\n"
                + "        \"systemImageRepositoryUrl\": \"docker-registry-default.cloud.registry.upshift.redhat.com\",\n"
                + "        \"systemImageType\": \"DOCKER_IMAGE\",\n" + "        \"podKeptOnFailure\": false,\n"
                + "        \"genericParameters\": {\n" + "            \"BUILDER_POD_MEMORY\": \"null\"\n"
                + "        },\n" + "        \"buildType\": \"MVN\",\n" + "        \"artifactRepositories\": [],\n"
                + "        \"tempBuild\": false,\n" + "        \"tempBuildTimestamp\": \"null\",\n"
                + "        \"scmTag\": null," + "        \"completionCallbackUrl\": \"URL\"}\n";

        ObjectMapper om = new ObjectMapper();
        BuildExecutionConfigurationWithCallbackRest obj = om
                .readValue(json, BuildExecutionConfigurationWithCallbackRest.class);

        System.out.println(obj);
    }

}
