/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jboss.pnc.client.patch.BuildConfigurationPatchBuilder;
import org.jboss.pnc.client.patch.ObjectMapperProvider;
import org.jboss.pnc.client.patch.PatchBuilderException;
import org.jboss.pnc.dto.BuildConfiguration;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;

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
