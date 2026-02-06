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
package org.jboss.pnc.facade.providers;

import java.net.URI;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;

import org.jboss.pnc.api.dto.Request;
import org.jboss.pnc.api.slsa.dto.provenance.v1.Provenance;
import org.jboss.pnc.common.http.PNCHttpClient;
import org.jboss.pnc.common.http.PNCHttpClientConfig;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.json.moduleconfig.slsa.BuilderConfig;
import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfigurationRevision;
import org.jboss.pnc.facade.util.SlsaProvenanceUtils;
import org.jboss.pnc.mapper.api.ArtifactMapper;
import org.jboss.pnc.mapper.api.BuildConfigurationRevisionMapper;
import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;

import lombok.NoArgsConstructor;

import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withMd5;
import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withSha1;
import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withSha256;
import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withBuildRecordId;
import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withDependantBuildRecordId;

import static org.jboss.pnc.common.util.StreamHelper.nullableStreamOf;

@PermitAll
@Stateless
public class SlsaProvenanceProviderHelper {

    private final PNCHttpClient httpClient = new PNCHttpClient(new SlsaConfig());

    private ArtifactRepository artifactRepository;

    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;

    private BuildMapper buildMapper;

    private ArtifactMapper artifactMapper;

    private BuildConfigurationRevisionMapper buildConfigurationRevisionMapper;

    public SlsaProvenanceProviderHelper() {
    }

    @Inject
    public SlsaProvenanceProviderHelper(
            ArtifactRepository artifactRepository,
            BuildConfigurationAuditedRepository buildConfigurationAuditedRepository,
            BuildMapper buildMapper,
            ArtifactMapper artifactMapper,
            BuildConfigurationRevisionMapper buildConfigurationRevisionMapper) {
        this.artifactRepository = artifactRepository;
        this.buildConfigurationAuditedRepository = buildConfigurationAuditedRepository;
        this.buildMapper = buildMapper;
        this.artifactMapper = artifactMapper;
        this.buildConfigurationRevisionMapper = buildConfigurationRevisionMapper;
    }

    public Artifact getArtifactById(String id) {
        org.jboss.pnc.model.Artifact artifact = artifactRepository.queryById(artifactMapper.getIdMapper().toEntity(id));
        if (artifact == null) {
            throw new NotFoundException();
        }
        return artifactMapper.toDTO(artifact);
    }

    public Artifact getArtifactByPurl(String purl) {
        org.jboss.pnc.model.Artifact artifact = artifactRepository.withPurl(purl);
        if (artifact == null) {
            throw new NotFoundException();
        }
        return artifactMapper.toDTO(artifact);
    }

    public List<Artifact> getAllArtifactsByDigest(String sha256, String md5, String sha1) {
        List<org.jboss.pnc.model.Artifact> artifacts = artifactRepository.queryWithPredicates(
                withSha256(Optional.ofNullable(sha256)),
                withMd5(Optional.ofNullable(md5)),
                withSha1(Optional.ofNullable(sha1)));
        return nullableStreamOf(artifacts).map(artifactMapper::toDTO).collect(Collectors.toList());
    }

    public BuildConfigurationRevision getBuildConfigRevisionByIdRev(String id, Integer rev) {
        BuildConfigurationAudited auditedBuildConfig = buildConfigurationAuditedRepository
                .queryById(new IdRev(Integer.valueOf(id), rev));
        return buildConfigurationRevisionMapper.toDTO(auditedBuildConfig);
    }

    public Collection<Artifact> getAllBuiltArtifacts(Build build) {
        List<org.jboss.pnc.model.Artifact> artifacts = artifactRepository
                .queryWithPredicates(withBuildRecordId(buildMapper.getIdMapper().toEntity(build.getId())));
        return nullableStreamOf(artifacts).map(artifactMapper::toDTO).collect(Collectors.toList());
    }

    public Collection<Artifact> getAllDependencies(Build build) {
        List<org.jboss.pnc.model.Artifact> artifacts = artifactRepository
                .queryWithPredicates(withDependantBuildRecordId(buildMapper.getIdMapper().toEntity(build.getId())));
        return nullableStreamOf(artifacts).map(artifactMapper::toDTO).collect(Collectors.toList());
    }

    public Provenance createBuildProvenance(Build build, GlobalModuleGroup globalConfig, BuilderConfig builderConfig) {

        BuildConfigurationRevision pncBuildConfigRevision = getBuildConfigRevisionByIdRev(
                build.getBuildConfigRevision().getId(),
                build.getBuildConfigRevision().getRev());
        Collection<Artifact> builtArtifacts = getAllBuiltArtifacts(build);
        Collection<Artifact> resolvedArtifacts = getAllDependencies(build);

        SlsaProvenanceUtils slsaProvenanceUtils = new SlsaProvenanceUtils(
                build,
                pncBuildConfigRevision,
                builtArtifacts,
                resolvedArtifacts,
                builderConfig,
                globalConfig,
                this::getBodyFromHttpRequest);

        return slsaProvenanceUtils.createBuildProvenance();
    }

    public Optional<String> getBodyFromHttpRequest(String url) {
        Request request = Request.builder().uri(URI.create(url)).method(Request.Method.GET).build();
        try {
            HttpResponse<String> response = httpClient.sendRequestForResponse(request);
            return Optional.ofNullable(response.body());
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }

    @NoArgsConstructor
    public class SlsaConfig implements PNCHttpClientConfig {
        RetryConfig retryConfig = new SlsaRetryConfig();

        @Override
        public RetryConfig retryConfig() {
            return retryConfig;
        }

        @Override
        public Duration connectTimeout() {
            return Duration.ofSeconds(5);
        }

        @Override
        public Duration requestTimeout() {
            return Duration.ofSeconds(4);
        }

        @Override
        public boolean forceHTTP11() {
            return false;
        }
    }

    @NoArgsConstructor
    public class SlsaRetryConfig implements PNCHttpClientConfig.RetryConfig {
        @Override
        public Duration backoffInitialDelay() {
            return Duration.ofSeconds(1);
        }

        @Override
        public Duration backoffMaxDelay() {
            return Duration.ofSeconds(6);
        }

        @Override
        public int maxRetries() {
            return 3;
        }

        @Override
        public Duration maxDuration() {
            return Duration.ofSeconds(20);
        }
    }

}
