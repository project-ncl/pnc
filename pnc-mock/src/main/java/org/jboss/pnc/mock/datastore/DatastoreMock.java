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
package org.jboss.pnc.mock.datastore;

import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.datastore.Datastore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.mockito.Mockito;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-24.
 */
@ApplicationScoped
public class DatastoreMock implements Datastore {

    private Logger log = LoggerFactory.getLogger(DatastoreMock.class.getName());

    private List<BuildRecord> buildRecords = Collections.synchronizedList(new ArrayList<>());

    private List<BuildConfigSetRecord> buildConfigSetRecords = Collections.synchronizedList(new ArrayList<>());

    private Map<Integer, BuildConfiguration> buildConfigurations = Collections.synchronizedMap(new HashMap<>());

    AtomicInteger buildRecordSetSequence = new AtomicInteger(0);
    AtomicInteger buildConfigAuditedRevSequence = new AtomicInteger(0);

    @Override
    public Map<Artifact, String> checkForBuiltArtifacts(Collection<Artifact> artifacts) {
        return new HashMap<>();
    }

    @Override
    public BuildRecord storeCompletedBuild(
            BuildRecord.Builder buildRecordBuilder,
            List<Artifact> builtArtifacts,
            List<Artifact> dependencies) {
        buildRecordBuilder.dependencies(dependencies);
        BuildRecord buildRecord = Mockito.spy(buildRecordBuilder.build());
        Mockito.when(buildRecord.getBuiltArtifacts()).thenReturn(new HashSet<>(builtArtifacts));
        BuildConfiguration buildConfiguration = buildRecord.getBuildConfigurationAudited().getBuildConfiguration();
        log.info("Storing build " + buildConfiguration);
        synchronized (this) {
            boolean exists = getBuildRecords().stream().anyMatch(br -> br.equals(buildRecord));
            if (exists) {
                throw new PersistenceException(
                        "Unique constraint violation, the record with id [" + buildRecord.getId()
                                + "] already exists.");
            }
            buildRecords.add(buildRecord);
        }
        return buildRecord;
    }

    @Override
    public BuildRecord storeRecordForNoRebuild(BuildRecord buildRecord) {
        buildRecords.add(buildRecord);
        return buildRecord;
    }

    @Override
    public User retrieveUserByUsername(String username) {
        User user = new User();
        user.setUsername("demo-user");
        return user;
    }

    public List<BuildRecord> getBuildRecords() {
        return new ArrayList<>(buildRecords); // avoid concurrent modification exception
    }

    public List<BuildConfigSetRecord> getBuildConfigSetRecords() {
        return buildConfigSetRecords;
    }

    @Override
    public void createNewUser(User user) {
    }

    @Override
    public BuildConfigSetRecord saveBuildConfigSetRecord(BuildConfigSetRecord buildConfigSetRecord) {
        if (buildConfigSetRecord.getId() == null) {
            buildConfigSetRecord.setId(buildRecordSetSequence.incrementAndGet());
        }
        log.info("Storing build config set record with id: " + buildConfigSetRecord);
        buildConfigSetRecords.add(buildConfigSetRecord);
        return buildConfigSetRecord;
    }

    @Override
    public BuildConfigurationAudited getLatestBuildConfigurationAudited(Integer buildConfigId) {
        BuildConfiguration buildConfig = buildConfigurations.get(buildConfigId);

        int rev = buildConfigAuditedRevSequence.incrementAndGet();
        BuildConfigurationAudited buildConfigurationAudited = BuildConfigurationAudited.Builder.newBuilder()
                .buildConfiguration(buildConfig)
                .rev(rev)
                .build();

        return buildConfigurationAudited;
    }

    @Override
    public BuildConfigurationAudited getLatestBuildConfigurationAuditedLoadBCDependencies(
            Integer buildConfigurationId) {
        BuildConfiguration buildConfig = buildConfigurations.get(buildConfigurationId);

        int rev = buildConfigAuditedRevSequence.incrementAndGet();
        BuildConfigurationAudited buildConfigurationAudited = BuildConfigurationAudited.Builder.newBuilder()
                .buildConfiguration(buildConfig)
                .rev(rev)
                .build();

        return buildConfigurationAudited;
    }

    @Override
    public BuildConfigSetRecord getBuildConfigSetRecordById(Integer buildConfigSetRecordId) {
        return buildConfigSetRecords.stream()
                .filter(bcsr -> bcsr.getId().equals(buildConfigSetRecordId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean requiresRebuild(BuildTask task, Set<Integer> processedDependenciesCache) {
        return true;
    }

    @Override
    public Set<BuildConfiguration> getBuildConfigurations(BuildConfigurationSet buildConfigurationSet) {
        return buildConfigurationSet.getBuildConfigurations();
    }

    @Override
    public boolean requiresRebuild(
            BuildConfigurationAudited buildConfigurationAudited,
            boolean checkImplicitDependencies,
            boolean temporaryBuild,
            Set<Integer> processedDependenciesCache,
            Consumer<BuildRecord> nonRebuildCauseSetter) {
        return true;
    }

    public BuildConfiguration save(BuildConfiguration buildConfig) {
        return buildConfigurations.put(buildConfig.getId(), buildConfig);
    }

    public void clear() {
        buildRecords.clear();
        buildConfigSetRecords.clear();
        buildConfigurations.clear();
        buildRecordSetSequence = new AtomicInteger(0);
        buildConfigAuditedRevSequence = new AtomicInteger(0);
    }
}
