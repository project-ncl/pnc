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
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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
@ApplicationScoped
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
     * @param buildRecord BuildRecord to be deleted
     */
    public void deleteTemporaryBuilds(BuildRecord buildRecord) {
        if (!buildRecord.isTemporaryBuild())
            throw new IllegalArgumentException("Only deletion of the temporary builds is allowed");
        log.info("Starting deletion of a temporary build " + buildRecord);

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

    public void deleteTemporaryBuildConfigSetRecord(BuildConfigSetRecord buildConfigSetRecord) {
        if (!buildConfigSetRecord.isTemporaryBuild())
            throw new IllegalArgumentException("Only deletion of the temporary builds is allowed");

        throw new NotImplementedException();
        // TODO
    }

    private void removeDependencyRelationBuildRecordArtifact(BuildRecord buildRecord, Set<Artifact> artifactsToBeDeleted,
                                                             boolean applyToBuiltArtifacts) {
        Set<Artifact> builtArtifacts = buildRecord.getBuiltArtifacts();
        for (Artifact artifact : builtArtifacts) {
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
