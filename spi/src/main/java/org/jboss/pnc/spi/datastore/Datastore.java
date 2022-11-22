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
package org.jboss.pnc.spi.datastore;

import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.coordinator.BuildTask;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Topmost datastore interface.
 */
public interface Datastore {

    /**
     * Check a list of artifacts if any of them was already built. The check is based on repository type and identifier.
     * It allows multiple same binaries with different identifier or different repository type.
     *
     * @param artifacts The artifacts to check
     * @return A Map containing the conflicting artifact and an error message, empty if there are no conflicts
     */
    Map<Artifact, String> checkForBuiltArtifacts(Collection<Artifact> artifacts);

    /**
     * Stores a completed build.
     *
     * @param buildRecordBuilder The build record builder which has been intialized with appropriate data.
     * @param builtArtifacts The list of artifacts built by the build.
     * @param dependencies The list of dependencies used by the build.
     * @return The updated BuildRecord
     * @throws DatastoreException Thrown if database is unable to process the request.
     */
    BuildRecord storeCompletedBuild(
            BuildRecord.Builder buildRecordBuilder,
            List<Artifact> builtArtifacts,
            List<Artifact> dependencies) throws DatastoreException;

    BuildRecord storeRecordForNoRebuild(BuildRecord buildRecord);

    /**
     * Returns User upon its username.
     *
     * @param username Username of the user.
     * @return User entity.
     */
    User retrieveUserByUsername(String username);

    /**
     * Creates new user.
     *
     * @param user User entity.
     */
    void createNewUser(User user);

    /**
     * Save build config set record to db
     *
     * @param buildConfigSetRecord The record to save
     * @return The updated BuildConfigSetRecord
     * @throws DatastoreException If there is a problem saving to the datastore
     */
    BuildConfigSetRecord saveBuildConfigSetRecord(BuildConfigSetRecord buildConfigSetRecord) throws DatastoreException;

    /**
     * Get the latest audited version of the given build configuration.
     *
     * @param buildConfigId The id of the config to check
     * @return The latest audited version of the build configuration
     */
    BuildConfigurationAudited getLatestBuildConfigurationAudited(Integer buildConfigId);

    /**
     * Get the latest audited version of the given build configuration and fetch whole dependency tree of the related BC
     *
     * @param buildConfigurationId The id of the config to check
     * @return The latest audited version of the build configuration with fetched dependency tree of the related BC
     */
    BuildConfigurationAudited getLatestBuildConfigurationAuditedLoadBCDependencies(Integer buildConfigurationId);

    BuildConfigSetRecord getBuildConfigSetRecordById(Integer buildConfigSetRecordId);

    /**
     * Check if a build configuration should be rebuilt (if some of its dependencies were rebuild or configuration was
     * modified)
     *
     * @param buildConfigurationAudited
     * @param checkImplicitDependencies when true check also automatically captured dependencies.
     * @param temporaryBuild true if requested build is going to be temporary
     * @return
     */
    default boolean requiresRebuild(
            BuildConfigurationAudited buildConfigurationAudited,
            boolean checkImplicitDependencies,
            boolean temporaryBuild,
            AlignmentPreference alignmentPreference,
            Set<Integer> processedDependenciesCache) {
        return requiresRebuild(
                buildConfigurationAudited,
                checkImplicitDependencies,
                temporaryBuild,
                alignmentPreference,
                processedDependenciesCache,
                ign -> {});
    }

    /**
     * Check if a build configuration should be rebuilt (if some of its dependencies were rebuild or configuration was
     * modified)
     *
     * @param buildConfigurationAudited
     * @param checkImplicitDependencies when true check also automatically captured dependencies.
     * @param temporaryBuild true if requested build is going to be temporary
     * @param nonRebuildCauseSetter this Consumer is used for setting a reference of BuildRecord causing not rebuilding
     * @return
     */
    boolean requiresRebuild(
            BuildConfigurationAudited buildConfigurationAudited,
            boolean checkImplicitDependencies,
            boolean temporaryBuild,
            AlignmentPreference alignmentPreference,
            Set<Integer> processedDependenciesCache,
            Consumer<BuildRecord> nonRebuildCauseSetter);

    @Deprecated
    boolean requiresRebuild(BuildTask task, Set<Integer> processedDependenciesCache);

    Set<BuildConfiguration> getBuildConfigurations(BuildConfigurationSet buildConfigurationSet);

    Collection<BuildConfigSetRecord> findBuildConfigSetRecordsInProgress();
}
