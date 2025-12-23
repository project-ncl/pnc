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

import java.util.Collection;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;

import org.jboss.pnc.api.slsa.dto.provenance.v1.Provenance;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.json.moduleconfig.slsa.BuilderConfig;
import org.jboss.pnc.common.json.moduleprovider.SlsaConfigProvider;
import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfigurationRevision;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.providers.SlsaProvenanceProviderHelper;
import org.jboss.pnc.facade.providers.SlsaProvenanceProviderHelper.DigestParts;
import org.jboss.pnc.rest.api.endpoints.SlsaProvenanceV1Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Getter;

@ApplicationScoped
public class SlsaProvenanceV1EndpointImpl implements SlsaProvenanceV1Endpoint {

    private static final Logger logger = LoggerFactory.getLogger(SlsaProvenanceV1EndpointImpl.class);

    public static final String PROVENANCE_UNAVAILABLE = "Build provenance cannot be provided.";

    @Inject
    @Getter
    private SlsaProvenanceProviderHelper slsaProvenanceProviderHelper;

    @Inject
    private GlobalModuleGroup globalConfig;

    @Inject
    private Configuration configuration;

    private BuilderConfig builderConfig;

    @PostConstruct
    public void init() throws ConfigurationParseException {
        this.builderConfig = configuration.getSlsaModuleConfig(new SlsaConfigProvider<>(BuilderConfig.class));
    }

    @Override
    public Provenance getFromBuildId(String id) {
        Build build = slsaProvenanceProviderHelper.getBuildById(id);
        if (build == null) {
            throw notFound(Build.class.getSimpleName(), "id", id);
        }
        if (Boolean.TRUE.equals(build.getTemporaryBuild())) {
            throw builtInTemporaryMode(Build.class.getSimpleName(), "id", id);
        }
        // If the build was not required, get the original build instead
        if (org.jboss.pnc.enums.BuildProgress.FINISHED.equals(build.getProgress())
                && org.jboss.pnc.enums.BuildStatus.NO_REBUILD_REQUIRED.equals(build.getStatus())) {

            build = slsaProvenanceProviderHelper.getBuildById(build.getNoRebuildCause().getId());
        }

        return createBuildProvenance(build);
    }

    @Override
    public Provenance getFromArtifactId(String id) {
        Artifact artifact = slsaProvenanceProviderHelper.getArtifactById(id);
        if (artifact == null) {
            throw notFound(Artifact.class.getSimpleName(), "id", id);
        }
        requireSuccessfulPersistentBuild(artifact.getBuild(), Artifact.class.getSimpleName(), "id", id);

        return createBuildProvenance(artifact.getBuild());
    }

    @Override
    public Provenance getFromArtifactDigest(String digest) {
        DigestParts digests = parseDigest(digest);
        Page<Artifact> artifacts = slsaProvenanceProviderHelper.getAllArtifactsByDigest(digests);
        if (artifacts.getTotalHits() == 0) {
            throw notFound(Artifact.class.getSimpleName(), "digest", digest);
        }

        Artifact artifact = artifacts.getContent()
                .stream()
                .filter(a -> a.getBuild() != null && !Boolean.TRUE.equals(a.getBuild().getTemporaryBuild()))
                .findFirst()
                .orElseThrow(
                        () -> badRequest(
                                String.format(
                                        "%s with digest: %s does not have an associated persistent build. %s",
                                        Artifact.class.getSimpleName(),
                                        digest,
                                        PROVENANCE_UNAVAILABLE)));

        return createBuildProvenance(artifact.getBuild());
    }

    @Override
    public Provenance getFromArtifactPurl(String purl) {
        Artifact artifact = slsaProvenanceProviderHelper.getArtifactByPurl(purl);
        if (artifact == null) {
            throw notFound(Artifact.class.getSimpleName(), "purl", purl);
        }
        requireSuccessfulPersistentBuild(artifact.getBuild(), Artifact.class.getSimpleName(), "purl", purl);

        return createBuildProvenance(artifact.getBuild());
    }

    public Provenance createBuildProvenance(Build build) {

        BuildConfigurationRevision pncBuildConfigRevision = slsaProvenanceProviderHelper.getBuildConfigRevisionByIdRev(
                build.getBuildConfigRevision().getId(),
                build.getBuildConfigRevision().getRev());
        Collection<Artifact> builtArtifacts = slsaProvenanceProviderHelper.fetchAllBuiltArtifacts(build);
        Collection<Artifact> resolvedArtifacts = slsaProvenanceProviderHelper.fetchAllDependencies(build);

        SlsaProvenanceUtils slsaProvenanceUtils = new SlsaProvenanceUtils(
                build,
                pncBuildConfigRevision,
                builtArtifacts,
                resolvedArtifacts,
                builderConfig,
                globalConfig,
                slsaProvenanceProviderHelper::getBodyFromHttpRequest);

        return slsaProvenanceUtils.createBuildProvenance();
    }

    private static RuntimeException notFound(String type, String prop, String value) {
        String reason = String.format("%s with %s: %s not found. %s", type, prop, value, PROVENANCE_UNAVAILABLE);
        logger.debug(reason);
        return new NotFoundException(reason);
    }

    private static RuntimeException badRequest(String reason) {
        logger.debug(reason);
        return new BadRequestException(reason);
    }

    private static RuntimeException notSuccessfulBuild(String type, String prop, String value) {
        return badRequest(
                String.format(
                        "%s with %s: %s is not successfully built in PNC. %s",
                        type,
                        prop,
                        value,
                        PROVENANCE_UNAVAILABLE));
    }

    private static RuntimeException notBuiltInPnc(String type, String prop, String value) {
        return badRequest(
                String.format("%s with %s: %s is not built in PNC. %s", type, prop, value, PROVENANCE_UNAVAILABLE));
    }

    private static RuntimeException builtInTemporaryMode(String type, String prop, String value) {
        return badRequest(
                String.format(
                        "%s with %s: %s is built in temporary mode. %s",
                        type,
                        prop,
                        value,
                        PROVENANCE_UNAVAILABLE));
    }

    private static void requireSuccessfulPersistentBuild(Build build, String type, String prop, String value) {
        if (build == null) {
            throw notBuiltInPnc(type, prop, value);
        }
        if (Boolean.TRUE.equals(build.getTemporaryBuild())) {
            throw builtInTemporaryMode(type, prop, value);
        }
        if (!(org.jboss.pnc.enums.BuildProgress.FINISHED.equals(build.getProgress())
                && (org.jboss.pnc.enums.BuildStatus.SUCCESS.equals(build.getStatus())
                        || org.jboss.pnc.enums.BuildStatus.NO_REBUILD_REQUIRED.equals(build.getStatus())))) {
            throw notSuccessfulBuild(type, prop, value);
        }
    }

    private static DigestParts parseDigest(String digest) {
        if (digest == null || digest.isBlank()) {
            throw badRequest("The digest provided is not valid.");
        }

        Optional<String> md5 = Optional.empty();
        Optional<String> sha1 = Optional.empty();
        Optional<String> sha256 = Optional.empty();

        if (digest.startsWith("md5:")) {
            md5 = Optional.of(digest.substring(4));
        } else if (digest.startsWith("sha1:")) {
            sha1 = Optional.of(digest.substring(5));
        } else if (digest.startsWith("sha256:")) {
            sha256 = Optional.of(digest.substring(7));
        } else {
            throw badRequest("The digest provided is not valid.");
        }

        if (!md5.isPresent() && !sha1.isPresent() && !sha256.isPresent()) {
            throw badRequest("The digest provided is not valid.");
        }

        if (md5.orElse(sha1.orElse(sha256.orElse(""))).isBlank()) {
            throw badRequest("The digest provided is not valid.");
        }

        return new DigestParts(sha256, sha1, md5);
    }

}
