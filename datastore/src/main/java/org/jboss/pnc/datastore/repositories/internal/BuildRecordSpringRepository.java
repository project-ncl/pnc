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

import java.util.List;

import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BuildRecordSpringRepository extends JpaRepository<BuildRecord, Integer>, JpaSpecificationExecutor<BuildRecord> {

    /**
     * Get all the revisions of a specific build configuration in order of newest to oldest.
     * 
     * @param id of the build configuration
     * @return The list of revisions of this build config in order of newest to oldest.
     */
    List<BuildRecord> findAllByLatestBuildConfigurationOrderByEndTimeDesc(BuildConfiguration buildConfiguration);

}
