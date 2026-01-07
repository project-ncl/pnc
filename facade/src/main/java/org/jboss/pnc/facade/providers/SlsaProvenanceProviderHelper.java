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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.pnc.api.dto.Request;
import org.jboss.pnc.common.http.PNCHttpClient;
import org.jboss.pnc.common.http.PNCHttpClientConfig;
import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfigurationRevision;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.providers.api.ArtifactProvider;
import org.jboss.pnc.facade.providers.api.BuildConfigurationProvider;
import org.jboss.pnc.facade.providers.api.BuildProvider;

import lombok.NoArgsConstructor;

import static org.jboss.pnc.rest.configuration.Constants.MAX_PAGE_SIZE;

@PermitAll
@Stateless
public class SlsaProvenanceProviderHelper {

    @Inject
    private ArtifactProvider artifactProvider;

    @Inject
    private BuildProvider buildProvider;

    @Inject
    private BuildConfigurationProvider buildConfigurationProvider;

    public Build getBuildById(String id) {
        return buildProvider.getSpecific(id);
    }

    public Artifact getArtifactById(String id) {
        return artifactProvider.getSpecific(id);
    }

    public Artifact getArtifactByPurl(String purl) {
        return artifactProvider.getSpecificFromPurl(purl);
    }

    public Page<Artifact> getAllArtifactsByDigest(DigestParts digests) {
        return artifactProvider
                .getAll(0, MAX_PAGE_SIZE, null, null, digests.getSha256(), digests.getMd5(), digests.getSha1());
    }

    public BuildConfigurationRevision getBuildConfigRevisionByIdRev(String id, Integer rev) {
        return buildConfigurationProvider.getRevision(id, rev);
    }

    public Collection<Artifact> fetchAllBuiltArtifacts(Build build) {
        return fetchAllPages(
                page -> artifactProvider.getBuiltArtifactsForBuild(page, MAX_PAGE_SIZE, null, null, build.getId()));
    }

    public Collection<Artifact> fetchAllDependencies(Build build) {
        return fetchAllPages(
                page -> artifactProvider.getDependantArtifactsForBuild(page, MAX_PAGE_SIZE, null, null, build.getId()));
    }

    @lombok.Value
    public static class DigestParts {
        Optional<String> sha256;
        Optional<String> sha1;
        Optional<String> md5;
    }

    public Optional<String> getBodyFromHttpRequest(String url) {
        PNCHttpClient client = new PNCHttpClient(new SlsaConfig());
        Request request = Request.builder().uri(URI.create(url)).method(Request.Method.GET).build();

        HttpResponse<String> response = client.sendRequestForResponse(request);
        if (response.statusCode() == 200) {
            return Optional.of(response.body());
        }
        return Optional.empty();
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
            return Duration.ofSeconds(30);
        }

        @Override
        public Duration requestTimeout() {
            return Duration.ofMinutes(2);
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
            return Duration.ofMinutes(1);
        }

        @Override
        public int maxRetries() {
            return -1;
        }

        @Override
        public Duration maxDuration() {
            return Duration.ofMinutes(20);
        }
    }

    private Collection<Artifact> fetchAllPages(java.util.function.IntFunction<Page<Artifact>> pageFetcher) {
        List<Artifact> artifacts = new ArrayList<Artifact>();

        int pageIndex = 0;
        while (true) {
            Page<Artifact> page = pageFetcher.apply(pageIndex);
            if (page == null || page.getContent() == null || page.getContent().isEmpty()) {
                break;
            }
            artifacts.addAll(page.getContent());
            if (page.getPageIndex() >= page.getTotalPages() - 1) {
                break;
            }
            pageIndex++;
        }
        return artifacts;
    }

}
