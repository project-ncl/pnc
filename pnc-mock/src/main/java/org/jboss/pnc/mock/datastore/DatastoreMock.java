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
package org.jboss.pnc.mock.datastore;

import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.datastore.Datastore;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-24.
 */
@ApplicationScoped
public class DatastoreMock implements Datastore {

    private Logger log = Logger.getLogger(DatastoreMock.class.getName());

    private List<BuildRecord> buildRecords = Collections.synchronizedList(new ArrayList<BuildRecord>());

    private List<BuildConfigSetRecord> buildConfigSetRecords = Collections.synchronizedList(new ArrayList<BuildConfigSetRecord>());
    
    private Map<Integer, BuildConfiguration> buildConfigurations = Collections.synchronizedMap(new HashMap<Integer, BuildConfiguration>());

    AtomicInteger buildRecordSequence = new AtomicInteger(0);
    AtomicInteger buildRecordSetSequence = new AtomicInteger(0);
    AtomicInteger buildConfigAuditedRevSequence = new AtomicInteger(0);

    @Override
    public BuildRecord storeCompletedBuild(BuildRecord buildRecord, Set<Integer> buildRecordSetIds ) {
        log.info("Storing build " + buildRecord.getLatestBuildConfiguration());
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
        return buildRecords;
    }

    public List<BuildConfigSetRecord> getBuildConfigSetRecords() {
        return buildConfigSetRecords;
    }

    @Override
    public void createNewUser(User user) {
    }

    @Override
    public int getNextBuildRecordId() {
        return buildRecordSequence.incrementAndGet();
    }

    @Override
    public BuildConfigSetRecord saveBuildConfigSetRecord(BuildConfigSetRecord buildConfigSetRecord) {
        if (buildConfigSetRecord.getId() == null) {
            buildConfigSetRecord.setId(buildRecordSetSequence.incrementAndGet());
        }
        log.info("Storing build config set record with id: " + buildConfigSetRecord.getId());
        buildConfigSetRecords.add(buildConfigSetRecord);
        return buildConfigSetRecord;
    }

    @Override
    public BuildConfigurationAudited getLatestBuildConfigurationAudited(Integer buildConfigId) {
        IdRev idRev = new IdRev();
        idRev.setId(buildConfigId);
        idRev.setRev(buildConfigAuditedRevSequence.incrementAndGet());
        BuildConfigurationAudited buildConfigAudited = new BuildConfigurationAudited();
        buildConfigAudited.setName("Audited config id: " + buildConfigId + " rev: " + idRev.getId());
        buildConfigAudited.setIdRev(idRev);
        buildConfigAudited.setBuildRecordId(idRev.getId());
        buildConfigAudited.setRev(idRev.getRev());
        
        BuildConfiguration buildConfig = buildConfigurations.get(buildConfigId);
        buildConfigAudited.setProject(buildConfig.getProject());
        buildConfigAudited.setBuildEnvironment(buildConfig.getBuildEnvironment());
        buildConfigAudited.setDescription(buildConfig.getDescription());

        return buildConfigAudited;
    }

    @Override
    public BuildConfigSetRecord getBuildConfigSetRecordById(Integer buildConfigSetRecordId) {
        return buildConfigSetRecords.stream().filter(bcsr -> bcsr.getId().equals(buildConfigSetRecordId)).findFirst().orElse(null);
    }

    @Override
    public boolean hasSuccessfulBuildRecord(BuildConfiguration buildConfiguration) {
        return buildRecords.stream()
                .filter(br -> br.getLatestBuildConfiguration().getId().equals(buildConfiguration.getId()))
                .map(br -> br.getStatus())
                .filter(status -> status == BuildStatus.SUCCESS)
                .count() > 0;
    }

    public BuildConfiguration save(BuildConfiguration buildConfig) {
        return buildConfigurations.put(buildConfig.getId(), buildConfig);
    }

    public void clear() {
        buildRecords.clear();
        buildConfigSetRecords.clear();
        buildConfigurations.clear();
        buildRecordSequence = new AtomicInteger(0);
        buildRecordSetSequence = new AtomicInteger(0);
        buildConfigAuditedRevSequence = new AtomicInteger(0);
    }
}
