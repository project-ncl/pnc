/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.client.patch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import org.assertj.core.api.Assertions;
import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.dto.BuildConfigurationRef;
import org.jboss.pnc.dto.ProjectRef;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildConfigurationPatchTest {

    private Logger logger = LoggerFactory.getLogger(BuildConfigurationPatchBuilder.class);
    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void shouldReplaceSimpleValue() throws PatchBuilderException, IOException, JsonPatchException {
        BuildConfiguration buildConfiguration = BuildConfiguration.builder()
                .id(1)
                .description("Hello Tom!")
                .build();

        String newDescription = "Hi Jerry";
        String patchString = new BuildConfigurationPatchBuilder().replaceDescription(newDescription).getJsonPatch();
        BuildConfiguration updatedBuildConfiguration = applyPatch(buildConfiguration, patchString);

        Assert.assertEquals(newDescription, updatedBuildConfiguration.getDescription());
    }

    @Test
    public void shouldReplaceRef() throws PatchBuilderException, IOException, JsonPatchException {
        ProjectRef project = ProjectRef.refBuilder()
                .id(1)
                .name("Project 1")
                .build();
        ProjectRef newProject = ProjectRef.refBuilder()
                .id(2)
                .name("Project 2")
                .build();
        BuildConfiguration buildConfiguration = BuildConfiguration.builder()
                .id(1)
                .name("BC 1")
                .project(project)
                .build();

        String patchString = new BuildConfigurationPatchBuilder()
                .replaceProject(newProject)
                .replaceName("Build Configuration 1")
                .getJsonPatch();
        BuildConfiguration updatedBuildConfiguration = applyPatch(buildConfiguration, patchString);

        Assert.assertEquals(newProject, updatedBuildConfiguration.getProject());
    }

    @Test
    public void shouldAddToMap() throws PatchBuilderException, IOException, JsonPatchException {
        Map<String, String> genericParameters = new HashMap<>();
        genericParameters.put("k", "v");
        BuildConfiguration buildConfiguration = BuildConfiguration.builder()
                .id(1)
                .parameters(genericParameters)
                .build();

        Map<String, String> addParameters = Collections.singletonMap("k2", "v2");
        String patchString = new BuildConfigurationPatchBuilder()
            .addParameters(addParameters)
            .getJsonPatch();
        BuildConfiguration updatedBuildConfiguration = applyPatch(buildConfiguration, patchString);

        genericParameters.putAll(addParameters);
        Assertions.assertThat(updatedBuildConfiguration.getParameters()).contains(genericParameters.entrySet().toArray(new Map.Entry[2]));
    }

    @Test
    public void shouldAddToCollection() throws PatchBuilderException, IOException, JsonPatchException {
        Set<BuildConfigurationRef> dependencies = new HashSet<>();
        BuildConfigurationRef dependency1 = BuildConfigurationRef.refBuilder().id(1).build();
        dependencies.add(dependency1);

        BuildConfiguration buildConfiguration = BuildConfiguration.builder()
                .id(1)
                .dependencies(dependencies)
                .build();

        Set<BuildConfigurationRef> addDependencies = new HashSet<>();
        BuildConfigurationRef dependency2 = BuildConfigurationRef.refBuilder().id(2).build();
        addDependencies.add(dependency2);
        String patchString = new BuildConfigurationPatchBuilder()
            .addDependencies(addDependencies)
            .getJsonPatch();
        BuildConfiguration updatedBuildConfiguration = applyPatch(buildConfiguration, patchString);

        dependencies.addAll(addDependencies);
        Assertions.assertThat(updatedBuildConfiguration.getDependencies()).contains(dependency1, dependency2);
    }

    @Test
    public void shouldReplaceCollection() throws PatchBuilderException, IOException, JsonPatchException {
        BuildConfigurationRef dependency1 = BuildConfigurationRef.refBuilder().id(1).build();
        BuildConfigurationRef dependency2 = BuildConfigurationRef.refBuilder().id(2).build();
        BuildConfigurationRef dependency2a = BuildConfigurationRef.refBuilder().id(2).build();
        BuildConfigurationRef dependency3 = BuildConfigurationRef.refBuilder().id(3).build();
        BuildConfigurationRef dependency4 = BuildConfigurationRef.refBuilder().id(4).build();
        Set<BuildConfigurationRef> dependencies = new HashSet<>();
        dependencies.add(dependency1);
        dependencies.add(dependency2);
        BuildConfiguration buildConfiguration = BuildConfiguration.builder()
                .id(1)
                .dependencies(dependencies)
                .build();

        Set<BuildConfigurationRef> newDependencies = new HashSet<>();
        newDependencies.add(dependency2a);
        newDependencies.add(dependency3);
        newDependencies.add(dependency4);
        String patchString = new BuildConfigurationPatchBuilder()
            .replaceDependencies(newDependencies)
            .getJsonPatch();
        BuildConfiguration updatedBuildConfiguration = applyPatch(buildConfiguration, patchString);

        Assertions.assertThat(updatedBuildConfiguration.getDependencies()).containsExactly(newDependencies.toArray(new BuildConfigurationRef[3]));
    }

    private BuildConfiguration applyPatch(BuildConfiguration buildConfiguration, String patchString)
            throws IOException, JsonPatchException {
        logger.info("Original: " + mapper.writeValueAsString(buildConfiguration));
        logger.info("Json patch:" + patchString);
        JsonPatch patch = JsonPatch.fromJson(mapper.readValue(patchString, JsonNode.class));
        JsonNode result = patch.apply(mapper.valueToTree(buildConfiguration));
        logger.info("Patched: " + mapper.writeValueAsString(result));
        return mapper.treeToValue(result, BuildConfiguration.class);
    }
}