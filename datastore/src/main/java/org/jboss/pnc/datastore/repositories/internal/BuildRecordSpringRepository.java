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
package org.jboss.pnc.datastore.repositories.internal;

import org.jboss.pnc.model.BuildRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;
import javax.enterprise.context.Dependent;

@Dependent
public interface BuildRecordSpringRepository
        extends JpaRepository<BuildRecord, Integer>, JpaSpecificationExecutor<BuildRecord> {

    @Query("select br from BuildRecord br fetch all properties where br.id = ?1")
    BuildRecord findByIdFetchAllProperties(Integer id);

    @Query("select br from BuildRecord br "
            + "left join fetch br.productMilestone "
            + "left join fetch br.buildConfigSetRecord "
            + "left join fetch br.user "
            + "where br.id = ?1")
    BuildRecord findByIdFetchProperties(Integer id);
    @Query("SELECT DISTINCT br.id, br.buildConfigurationId, br.buildConfigurationRev FROM BuildRecord br "
            + "JOIN br.builtArtifacts builtArtifacts "
            + "WHERE builtArtifacts.id IN (?1)")
    Set<BuildRecord> findByBuiltArtifacts(Set<Integer> dependenciesIds);
}
