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
package org.jboss.pnc.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import org.jboss.pnc.client.patch.BuildConfigurationPatchBuilder;
import org.jboss.pnc.client.patch.ObjectMapperProvider;
import org.jboss.pnc.client.patch.PatchBuilderException;
import org.jboss.pnc.dto.BuildConfiguration;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildConfigurationSerializationTest {

    @Test
    public void shouldPatchBuildConfiguration() throws PatchBuilderException, IOException, JsonPatchException {
        ObjectMapper mapper = ObjectMapperProvider.getInstance();

        // given
        Instant now = Instant.now();
        Map<String, String> initialParameters = Collections.singletonMap("KEY", "VALUE");
        BuildConfiguration buildConfiguration = BuildConfiguration.builder()
                .id("1")
                .name("name")
                .creationTime(now)
                .parameters(initialParameters)
                .build();

        // when
        BuildConfigurationPatchBuilder patchBuilder = new BuildConfigurationPatchBuilder();
        patchBuilder.replaceName("new name");
        Map<String, String> newParameter = Collections.singletonMap("KEY 2", "VALUE 2");
        patchBuilder.addParameters(newParameter);

        JsonNode targetJson = mapper.valueToTree(buildConfiguration);
        JsonPatch patch = JsonPatch.fromJson(mapper.readValue(patchBuilder.getJsonPatch(), JsonNode.class));
        JsonNode result = patch.apply(targetJson);

        // then
        BuildConfiguration deserialized = mapper.treeToValue(result, BuildConfiguration.class);
        Assert.assertEquals(now, deserialized.getCreationTime());
        Assert.assertEquals("new name", deserialized.getName());

        Map<String, String> finalParameters = new HashMap<>(initialParameters);
        finalParameters.putAll(newParameter);
        assertThat(deserialized.getParameters()).containsAllEntriesOf(finalParameters);
    }

}
