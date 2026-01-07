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
package org.jboss.pnc.rest.endpoints;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jboss.pnc.api.dto.ComponentVersion;
import org.jboss.pnc.api.enums.slsa.BuildSystem;
import org.jboss.pnc.api.slsa.dto.provenance.v1.BuildDefinition;
import org.jboss.pnc.api.slsa.dto.provenance.v1.Builder;
import org.jboss.pnc.api.slsa.dto.provenance.v1.Metadata;
import org.jboss.pnc.api.slsa.dto.provenance.v1.Predicate;
import org.jboss.pnc.api.slsa.dto.provenance.v1.Provenance;
import org.jboss.pnc.api.slsa.dto.provenance.v1.ResourceDescriptor;
import org.jboss.pnc.api.slsa.dto.provenance.v1.RunDetails;
import org.jboss.pnc.common.Json;
import org.jboss.pnc.common.Strings;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.json.moduleconfig.slsa.BuilderConfig;
import org.jboss.pnc.common.json.moduleconfig.slsa.ProvenanceEntry;
import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfigurationRevision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

import static org.jboss.pnc.api.constants.slsa.ProvenanceKeys.*;

@AllArgsConstructor
public class SlsaProvenanceUtils {

    private static final Logger logger = LoggerFactory.getLogger(SlsaProvenanceUtils.class);

    private static final String SLSLA_BUILD_PROVENANCE_ATTESTATION_TYPE = "https://in-toto.io/Statement/v1";
    private static final String SLSLA_BUILD_PROVENANCE_PREDICATE_TYPE = "https://slsa.dev/provenance/v1";

    private final Build pncBuild;
    private final BuildConfigurationRevision pncBuildConfigRevision;
    private final Collection<Artifact> builtArtifacts;
    private final Collection<Artifact> resolvedArtifacts;
    private final BuilderConfig builderConfig;
    private final GlobalModuleGroup globalConfig;
    private final Function<String, Optional<String>> urlInvoker;

    public Provenance createBuildProvenance() {

        List<ResourceDescriptor> subject = createResourceDescriptors(builtArtifacts);
        List<ResourceDescriptor> resolvedDependencies = createResolvedDependencies(pncBuild, resolvedArtifacts);

        Map<String, Object> externalParameters = createExternalParameters(pncBuild, pncBuildConfigRevision);

        // TODO: add rebuild mode once https://issues.redhat.com/browse/NCL-7025 is done
        Map<String, Object> internalParameters = Map.of(
                PROVENANCE_V1_BUILD_DETAILS_DEFAULT_ALIGN_PARAMETERS,
                pncBuildConfigRevision.getDefaultAlignmentParams());

        BuildDefinition buildDefinition = BuildDefinition.builder()
                .buildType(BuildSystem.PNC.getBuildType())
                .externalParameters(externalParameters)
                .internalParameters(internalParameters)
                .resolvedDependencies(resolvedDependencies)
                .build();

        Metadata metadata = Metadata.builder()
                .invocationId(pncBuild.getId())
                .startedOn(pncBuild.getSubmitTime())
                .finishedOn(pncBuild.getEndTime())
                .build();

        Builder builder = Builder.newBuilder()
                .id(createBuilderId(pncBuild, builderConfig))
                .version(createComponentVersionsMap(pncBuild, builderConfig))
                .build();

        List<ResourceDescriptor> byproducts = createByproducts(pncBuild, builderConfig);
        RunDetails runDetails = RunDetails.builder().builder(builder).metadata(metadata).byproducts(byproducts).build();
        Predicate predicate = Predicate.builder().buildDefinition(buildDefinition).runDetails(runDetails).build();

        return Provenance.builder()
                .type(SLSLA_BUILD_PROVENANCE_ATTESTATION_TYPE)
                .subject(subject)
                .predicateType(SLSLA_BUILD_PROVENANCE_PREDICATE_TYPE)
                .predicate(predicate)
                .build();
    }

    /**
     * Convert a collection of artifacts into resource descriptors.
     */
    private List<ResourceDescriptor> createResourceDescriptors(Collection<Artifact> artifacts) {
        return artifacts.stream().map((Artifact artifact) -> {
            Map<String, Object> annotations = new HashMap<>();

            if (artifact.getIdentifier() != null) {
                annotations.put(PROVENANCE_V1_ARTIFACT_IDENTIFIER, artifact.getIdentifier());
            }
            if (artifact.getPurl() != null) {
                annotations.put(PROVENANCE_V1_ARTIFACT_PURL, artifact.getPurl());
            }
            if (artifact.getPublicUrl() != null) {
                annotations.put(PROVENANCE_V1_ARTIFACT_URI, artifact.getPublicUrl());
            }

            return ResourceDescriptor.builder()
                    .name(artifact.getFilename())
                    .digest(Map.of(PROVENANCE_V1_ARTIFACT_SHA256, artifact.getSha256()))
                    .annotations(annotations)
                    .build();
        }).collect(Collectors.toList());

    }

    private List<ResourceDescriptor> createResolvedDependencies(
            Build pncBuild,
            Collection<Artifact> resolvedArtifacts) {

        List<ResourceDescriptor> deps = new ArrayList<ResourceDescriptor>();

        if (!Strings.isEmpty(pncBuild.getScmBuildConfigRevision()) && pncBuild.getScmRepository() != null
                && !Strings.isEmpty(pncBuild.getScmRepository().getExternalUrl())) {
            deps.add(
                    ResourceDescriptor.builder()
                            .name(PROVENANCE_V1_SCM_REPOSITORY)
                            .digest(Map.of(PROVENANCE_V1_SCM_COMMIT, pncBuild.getScmBuildConfigRevision()))
                            .uri(pncBuild.getScmRepository().getExternalUrl())
                            .build());
        }

        if (!Strings.isEmpty(pncBuild.getScmRevision()) && !Strings.isEmpty(pncBuild.getScmUrl())
                && !Strings.isEmpty(pncBuild.getScmTag())) {
            deps.add(
                    ResourceDescriptor.builder()
                            .name(PROVENANCE_V1_SCM_DOWNSTREAM_REPOSITORY)
                            .digest(Map.of(PROVENANCE_V1_SCM_COMMIT, pncBuild.getScmRevision()))
                            .uri(pncBuild.getScmUrl())
                            .annotations(Map.of(PROVENANCE_V1_SCM_TAG, pncBuild.getScmTag()))
                            .build());
        }

        if (!Strings.isEmpty(pncBuild.getEnvironment().getSystemImageRepositoryUrl())
                && !Strings.isEmpty(pncBuild.getEnvironment().getSystemImageId())) {
            deps.add(
                    ResourceDescriptor.builder()
                            .name(PROVENANCE_V1_ENVIRONMENT)
                            .uri(
                                    pncBuild.getEnvironment().getSystemImageRepositoryUrl() + "/"
                                            + pncBuild.getEnvironment().getSystemImageId())
                            .build());
        }

        deps.addAll(createResourceDescriptors(resolvedArtifacts));
        return deps;
    }

    private Map<String, Object> createExternalParameters(
            org.jboss.pnc.dto.Build pncBuild,
            BuildConfigurationRevision rev) {

        // Merge build parameters and include extra flags
        Map<String, Object> mergedParameters = new HashMap<>(rev.getParameters());
        mergedParameters.put(PROVENANCE_V1_BUILD_DETAILS_BREW_PULL_ACTIVE, String.valueOf(rev.isBrewPullActive()));

        // Build details map with all relevant metadata
        Map<String, Object> buildDetails = Map.of(
                PROVENANCE_V1_BUILD_DETAILS_TYPE,
                rev.getBuildType().toString(),
                PROVENANCE_V1_BUILD_DETAILS_TEMPORARY,
                String.valueOf(pncBuild.getTemporaryBuild()),
                PROVENANCE_V1_BUILD_DETAILS_SCRIPT,
                rev.getBuildScript(),
                PROVENANCE_V1_BUILD_DETAILS_NAME,
                rev.getName(),
                PROVENANCE_V1_BUILD_DETAILS_PARAMETERS,
                mergedParameters);

        return Map.of(
                PROVENANCE_V1_BUILD,
                buildDetails,
                PROVENANCE_V1_SCM_REPOSITORY,
                Map.of(
                        PROVENANCE_V1_URI,
                        pncBuild.getScmRepository().getExternalUrl(),
                        PROVENANCE_V1_REVISION,
                        rev.getScmRevision(),
                        PROVENANCE_V1_PRE_BUILD_SYNC,
                        String.valueOf(rev.getScmRepository().getPreBuildSyncEnabled())),
                PROVENANCE_V1_ENVIRONMENT,
                Map.of(PROVENANCE_V1_NAME, pncBuild.getEnvironment().getName()));
    }

    private String createBuilderId(Build pncBuild, BuilderConfig builderConfig) {
        ProvenanceEntry componentEntry = builderConfig.getId();
        if (componentEntry != null) {
            Optional<String> componentEntryResolvedValue = resolveComponentUriOrValue(componentEntry, pncBuild);
            if (componentEntryResolvedValue.isPresent()) {
                return componentEntryResolvedValue.get();
            }
        }
        return pncBuild.getId();
    }

    private List<ResourceDescriptor> createByproducts(Build pncBuild, BuilderConfig builderConfig) {
        List<ResourceDescriptor> descriptors = new ArrayList<>();
        if (builderConfig.getByProducts() != null) {
            for (ProvenanceEntry componentEntry : builderConfig.getByProducts()) {
                Optional<String> componentEntryResolvedValue = resolveComponentUriOrValue(componentEntry, pncBuild);
                if (componentEntryResolvedValue.isPresent()) {
                    descriptors.add(
                            ResourceDescriptor.builder()
                                    .name(componentEntry.getProvenanceEntryName())
                                    .uri(componentEntryResolvedValue.get())
                                    .build());
                }
            }
        }
        return descriptors;
    }

    private Map<String, String> createComponentVersionsMap(Build pncBuild, BuilderConfig builderConfig) {
        Map<String, String> versions = new HashMap<String, String>();
        if (builderConfig.getComponentVersions() != null) {
            for (ProvenanceEntry componentEntry : builderConfig.getComponentVersions()) {
                Optional<String> componentEntryResolvedValue = resolveComponentUriOrValue(componentEntry, pncBuild);
                if (componentEntryResolvedValue.isPresent()) {
                    versions.put(componentEntry.getProvenanceEntryName(), componentEntryResolvedValue.get());
                }
            }
        }
        return versions;
    }

    private String invokeGetterOnGlobalConfig(String fieldName) {
        try {
            String getterName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
            Method getter = globalConfig.getClass().getMethod(getterName);
            return String.valueOf(getter.invoke(globalConfig));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<String> resolveComponentUriOrValue(ProvenanceEntry entry, Build pncBuild) {
        String baseUrl = invokeGetterOnGlobalConfig(entry.getGlobalConfigUrlRef());
        String fullUrl = baseUrl + entry.getUrlSuffix();
        String resolverMethod = entry.getResolverMethod();

        if ("invoke".equals(resolverMethod)) {
            Optional<String> responseBody = urlInvoker.apply(fullUrl);
            return parseVersionFromHttResponseBody(responseBody);
        }

        if ("replace".equals(resolverMethod)) {
            return Optional.of(fullUrl.replace(BuilderConfig.REPLACE_TOKEN, pncBuild.getId()));
        }

        return Optional.of(fullUrl);
    }

    private Optional<String> parseVersionFromHttResponseBody(Optional<String> responseBody) {
        if (responseBody.isEmpty()) {
            return Optional.empty();
        }
        ObjectMapper objectMapper = Json.newObjectMapper();
        try {
            Optional.of(objectMapper.readValue(responseBody.get(), ComponentVersion.class));
        } catch (IOException e) {
            logger.error("Error while casting the response body to ComponentVersion, will attempt a different cast", e);
            try {
                JsonNode root = objectMapper.readTree(responseBody.get());
                return Optional.of(root.path("version").asText());
            } catch (IOException exc) {
                logger.error("Failed to parse invoke response JSON", exc);
            }
        }

        return Optional.empty();
    }

}
