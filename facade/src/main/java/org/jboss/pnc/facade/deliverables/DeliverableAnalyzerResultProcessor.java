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
package org.jboss.pnc.facade.deliverables;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.jboss.pnc.api.deliverablesanalyzer.dto.Artifact;
import org.jboss.pnc.api.deliverablesanalyzer.dto.ArtifactType;
import org.jboss.pnc.api.deliverablesanalyzer.dto.Build;
import org.jboss.pnc.api.deliverablesanalyzer.dto.MavenArtifact;
import org.jboss.pnc.api.deliverablesanalyzer.dto.NPMArtifact;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.mapper.api.ArtifactMapper;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.TargetRepository;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;
import org.jboss.pnc.spi.datastore.repositories.TargetRepositoryRepository;

import static org.jboss.pnc.constants.ReposiotryIdentifier.DISTRIBUTION_ARCHIVE;
import static org.jboss.pnc.constants.ReposiotryIdentifier.INDY_MAVEN;

/**
 *
 * @author jbrazdil
 */
@Transactional
public class DeliverableAnalyzerResultProcessor {
    private static final String KOJI_PATH_MAVEN_PREFIX = "/api/content/maven/remote/koji-";

    @Inject
    private ProductMilestoneRepository milestoneRepository;
    @Inject
    private ArtifactRepository artifactRepository;
    @Inject
    private TargetRepositoryRepository targetRepositoryRepository;
    @Inject
    private ArtifactMapper artifactMapper;

    /**
     * Processes the result of anylysis of delivarables and stores the artifacts as distributed artifacts of Product
     * Milestone.
     *
     * @param milestoneId Id of the milestone to which the distributed artifact will be stored.
     * @param builds List of builds from the analyis result.
     * @param distributionUrl URL of the distribution file.
     * @param user User who initialized the import.
     */
    public void processDeliverables(int milestoneId, Collection<Build> builds, String distributionUrl, User user) {
        ProductMilestone milestone = milestoneRepository.queryById(milestoneId);
        for (Build build : builds) {
            Function<Artifact, org.jboss.pnc.model.Artifact> artifactParser;
            if (build.getBuildSystemType() == null) {
                TargetRepository distributionRepository = getDistributionRepository(distributionUrl);
                artifactParser = art -> findOrCreateArtifact(art, distributionRepository);
            } else {
                switch (build.getBuildSystemType()) {
                    case PNC:
                        artifactParser = this::getPncArtifact;
                        break;
                    case BREW:
                        TargetRepository brewRepository = getBrewRepository(build);
                        artifactParser = art -> findOrCreateArtifact(assertBrewArtifacts(art), brewRepository);
                        break;
                    default:
                        throw new UnsupportedOperationException(
                                "Unknown build system type " + build.getBuildSystemType());
                }
            }
            build.getArtifacts().stream().map(artifactParser).forEach(milestone::addDistributedArtifact);
        }
        milestone.setDistributedArtifactsImporter(user);
    }

    @SuppressWarnings("unchecked")
    private org.jboss.pnc.model.Artifact findOrCreateArtifact(Artifact art, TargetRepository targetRepo) {
        org.jboss.pnc.model.Artifact artifact = mapArtifact(art);
        // find
        org.jboss.pnc.model.Artifact dbArtifact = artifactRepository.queryByPredicates(
                ArtifactPredicates.withIdentifierAndSha256(artifact.getIdentifier(), artifact.getSha256()),
                ArtifactPredicates.withTargetRepositoryId(targetRepo.getId()));
        if (dbArtifact != null) {
            return dbArtifact;
        }

        // create
        artifact.setTargetRepository(targetRepo);
        org.jboss.pnc.model.Artifact savedArtifact = artifactRepository.save(artifact);
        targetRepo.getArtifacts().add(savedArtifact);
        return savedArtifact;
    }

    private org.jboss.pnc.model.Artifact getPncArtifact(Artifact art) {
        org.jboss.pnc.model.Artifact artifact = artifactRepository
                .queryById(artifactMapper.getIdMapper().toEntity(art.getPncId()));
        if (artifact == null) {
            throw new IllegalArgumentException("PNC artifact with id " + art.getPncId() + " doesn't exist.");
        }
        return artifact;
    }

    private org.jboss.pnc.model.Artifact mapArtifact(Artifact art) {
        org.jboss.pnc.model.Artifact.Builder builder = org.jboss.pnc.model.Artifact.builder();
        builder.md5(art.getMd5());
        builder.sha1(art.getSha1());
        builder.sha256(art.getSha256());
        builder.size(art.getSize());
        builder.filename(art.getFilename());
        if (art.getArtifactType() == null) {
            builder.identifier(art.getFilename());
        } else
            switch (art.getArtifactType()) {
                case MAVEN:
                    builder.identifier(fill((MavenArtifact) art));
                    break;
                case NPM:
                    builder.identifier(fill((NPMArtifact) art));
                    break;
            }
        if (art.isBuiltFromSource()) {
            builder.artifactQuality(ArtifactQuality.NEW);
        } else {
            builder.artifactQuality(ArtifactQuality.IMPORTED);
        }

        return builder.build();
    }

    private String fill(MavenArtifact mavenArtifact) {
        return Arrays
                .asList(
                        mavenArtifact.getGroupId(),
                        mavenArtifact.getArtifactId(),
                        mavenArtifact.getType(),
                        mavenArtifact.getVersion(),
                        mavenArtifact.getClassifier())
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.joining(":"));
    }

    private String fill(NPMArtifact mavenArtifact) {
        return mavenArtifact.getName() + ":" + mavenArtifact.getVersion();
    }

    private TargetRepository getBrewRepository(Build build) {
        String path = KOJI_PATH_MAVEN_PREFIX + build.getBrewNVR();
        TargetRepository tr = targetRepositoryRepository.queryByIdentifierAndPath(INDY_MAVEN, path);
        if (tr == null) {
            tr = createRepository(path, INDY_MAVEN, RepositoryType.MAVEN);
        }
        return tr;
    }

    private TargetRepository getDistributionRepository(String distURL) {
        TargetRepository tr = targetRepositoryRepository.queryByIdentifierAndPath(DISTRIBUTION_ARCHIVE, distURL);
        if (tr == null) {
            tr = createRepository(distURL, DISTRIBUTION_ARCHIVE, RepositoryType.DISTRIBUTION_ARCHIVE);
        }
        return tr;
    }

    private TargetRepository createRepository(String path, String identifier, RepositoryType type) {
        TargetRepository tr = TargetRepository.newBuilder()
                .temporaryRepo(false)
                .identifier(identifier)
                .repositoryPath(path)
                .repositoryType(type)
                .build();
        return targetRepositoryRepository.save(tr);
    }

    private Artifact assertBrewArtifacts(Artifact artifact) {
        if (!(artifact.getArtifactType() == null || artifact.getArtifactType() == ArtifactType.MAVEN)) {
            throw new IllegalArgumentException(
                    "Brew artifacts are expected to be either MAVEN or unknown, artifact " + artifact + " is "
                            + artifact.getArtifactType());
        }
        return artifact;
    }

}
