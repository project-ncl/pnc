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

import java.util.List;

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
import org.jboss.pnc.facade.providers.SlsaProvenanceProviderHelper;
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
    public Provenance getFromArtifactId(String id) {
        Artifact artifact = slsaProvenanceProviderHelper.getArtifactById(id);
        if (artifact == null) {
            throw notFound("id", id);
        }

        if (artifact.getBuild() == null) {
            throw notBuiltInPnc("id", id);
        }

        return slsaProvenanceProviderHelper.createBuildProvenance(artifact.getBuild(), globalConfig, builderConfig);
    }

    @Override
    public Provenance getFromArtifactDigest(String sha256, String md5, String sha1) {

        List<Artifact> artifacts = slsaProvenanceProviderHelper.getAllArtifactsByDigest(sha256, md5, sha1);
        if (artifacts.size() == 0) {
            throw notFound("sha256|sha1|md5", sha256 + "|" + md5 + "|" + sha1);
        }

        Artifact artifact = artifacts.stream()
                .filter(a -> a.getBuild() != null)
                .findFirst()
                .orElseThrow(() -> notBuiltInPnc("sha256|sha1|md5", sha256 + "|" + md5 + "|" + sha1));

        return slsaProvenanceProviderHelper.createBuildProvenance(artifact.getBuild(), globalConfig, builderConfig);
    }

    @Override
    public Provenance getFromArtifactPurl(String purl) {
        Artifact artifact = slsaProvenanceProviderHelper.getArtifactByPurl(purl);
        if (artifact == null) {
            throw notFound("purl", purl);
        }

        if (artifact.getBuild() == null) {
            throw notBuiltInPnc("purl", purl);
        }
        return slsaProvenanceProviderHelper.createBuildProvenance(artifact.getBuild(), globalConfig, builderConfig);
    }

    private static RuntimeException notFound(String prop, String value) {
        String reason = String.format("Artifact with %s: %s not found. %s", prop, value, PROVENANCE_UNAVAILABLE);
        logger.debug(reason);
        return new NotFoundException(reason);
    }

    private static RuntimeException notBuiltInPnc(String prop, String value) {
        String reason = String
                .format("Artifact with %s: %s is not built in PNC. %s", prop, value, PROVENANCE_UNAVAILABLE);
        logger.debug(reason);
        return new BadRequestException(reason);
    }

}
