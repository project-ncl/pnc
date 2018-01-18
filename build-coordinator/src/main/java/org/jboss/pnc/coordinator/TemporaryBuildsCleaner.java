/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.coordinator;

import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigSetRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Bean providing an interface to delete temporary builds
 *
 * @author Jakub Bartecek
 */
@Stateless
public class TemporaryBuildsCleaner {
    private final Logger log = LoggerFactory.getLogger(TemporaryBuildsCleaner.class);

    private BuildRecordRepository buildRecordRepository;

    private BuildConfigSetRecordRepository buildConfigSetRecordRepository;

    private ArtifactRepository artifactRepository;

    @Deprecated
    public TemporaryBuildsCleaner() {
    }

    @Inject
    public TemporaryBuildsCleaner(BuildRecordRepository buildRecordRepository, BuildConfigSetRecordRepository buildConfigSetRecordRepository,
                                  ArtifactRepository artifactRepository) {
        this.buildRecordRepository = buildRecordRepository;
        this.buildConfigSetRecordRepository = buildConfigSetRecordRepository;
        this.artifactRepository = artifactRepository;
    }

    /**
     * Deletes a temporary build and artifacts created during the build or orphan dependencies used
     *
     * @param detachedBuildRecord BuildRecord to be deleted
     */
    public void deleteTemporaryBuild(BuildRecord detachedBuildRecord) {
        if (!detachedBuildRecord.isTemporaryBuild())
            throw new IllegalArgumentException("Only deletion of the temporary builds is allowed");
        BuildRecord buildRecord = buildRecordRepository.findByIdFetchAllProperties(detachedBuildRecord.getId());
        log.info("Starting deletion of a temporary build " + buildRecord + "; Built artifacts: " + buildRecord.getBuiltArtifacts()
                + "; Dependencies: " + buildRecord.getDependencies());

        /** Delete relation between BuildRecord and Artifact */
        Set<Artifact> artifactsToBeDeleted = new HashSet<>();

        removeDependencyRelationBuildRecordArtifact(buildRecord, artifactsToBeDeleted, true);
        removeDependencyRelationBuildRecordArtifact(buildRecord, artifactsToBeDeleted, false);

        /**
         * Delete artifacts, if the artifacts are not used in other builds
         */
        for(Artifact artifact : artifactsToBeDeleted) {
            log.info("Deleting temporary artifact: " + artifact.getDescriptiveString());
            artifactRepository.delete(artifact.getId());
        }

        buildRecordRepository.delete(buildRecord.getId());
    }

    /**
     * Deletes a BuildConfigSetRecord and BuildRecords produced in the build
     *
     * @param detachedBuildConfigSetRecord BuildConfigSetRecord to be deleted
     */
    public void deleteTemporaryBuildConfigSetRecord(BuildConfigSetRecord detachedBuildConfigSetRecord) {
        if (!detachedBuildConfigSetRecord.isTemporaryBuild())
            throw new IllegalArgumentException("Only deletion of the temporary builds is allowed");
        BuildConfigSetRecord buildConfigSetRecord = buildConfigSetRecordRepository.queryById(detachedBuildConfigSetRecord.getId());
        log.info("Starting deletion of a temporary build record set " + buildConfigSetRecord);

        buildConfigSetRecord.getBuildRecords().forEach(br -> deleteTemporaryBuild(br));
        buildConfigSetRecordRepository.delete(buildConfigSetRecord.getId());

        log.info("Deletion of a temporary build record set finished: " + buildConfigSetRecord);
    }

    private void removeDependencyRelationBuildRecordArtifact(BuildRecord buildRecord, Set<Artifact> artifactsToBeDeleted,
                                                             boolean applyToBuiltArtifacts) {
        Set<Artifact> artifacts;
        if (applyToBuiltArtifacts){
            artifacts = buildRecord.getBuiltArtifacts();
        }
        else {
            artifacts = buildRecord.getDependencies();
        }

        for (Artifact artifact : artifacts) {
            log.info(String.format("Deleting relation BR-Artifact. BuiltArtifacts? %s, BR=%s, artifact=%s", applyToBuiltArtifacts,
                    buildRecord, artifact.getDescriptiveString()));

            if (artifact.getDistributedInProductMilestones().size() != 0) {
                log.error("Temporary artifact was distributed in milestone! Artifact: "
                        + artifact.toString() + "\n Milestones: " + artifact.getDistributedInProductMilestones().toString());
                continue;
            }

            if (applyToBuiltArtifacts)
                artifact.getBuildRecords().remove(buildRecord);
            else
                artifact.getDependantBuildRecords().remove(buildRecord);

            artifactRepository.save(artifact);
            artifactsToBeDeleted.add(artifact);
        }

        if (applyToBuiltArtifacts)
            buildRecord.setBuiltArtifacts(Collections.emptySet());
        else
            buildRecord.setDependencies(Collections.emptySet());

        buildRecordRepository.save(buildRecord);
    }

}
