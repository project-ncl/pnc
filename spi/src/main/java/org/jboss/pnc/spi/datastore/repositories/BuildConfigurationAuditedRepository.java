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
package org.jboss.pnc.spi.datastore.repositories;

import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.Project;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface for manipulating {@link org.jboss.pnc.model.BuildConfigurationAudited} entity.
 */
public interface BuildConfigurationAuditedRepository {
    List<BuildConfigurationAudited> findAllByIdOrderByRevDesc(Integer id);

    /**
     * Finds latest revision of a Build Config with given ID.
     * 
     * @param buildConfigurationId ID of the Build Config.
     * @return Latest audited revision of the BC or null if the BC such exists.
     */
    BuildConfigurationAudited findLatestById(int buildConfigurationId);

    /**
     * Lookups a BuildConfigurationAudited entity
     *
     * @param idRev Id and Revision of a desired BuildConfigurationAudited entity
     * @return BuildConfigurationAudited or null if there is no such entity
     */
    BuildConfigurationAudited queryById(IdRev idRev);

    Map<IdRev, BuildConfigurationAudited> queryById(Set<IdRev> idRev);

    /**
     * Searches for audited BuildConfigurations by BuildConfig name with support for like operation with syntax *name*
     * 
     * @param buildConfigurationName Search pattern
     * @return Found BCAs
     */
    List<BuildConfigurationAudited> searchForBuildConfigurationName(String buildConfigurationName);

    /**
     * Searches for IdRevs by BuildConfig name with support for like operation with syntax *name*
     * 
     * @param buildConfigurationName Search pattern
     * @return Found IdRevs
     */
    List<IdRev> searchIdRevForBuildConfigurationName(String buildConfigurationName);

    List<IdRev> searchIdRevForBuildConfigurationNameOrProjectName(List<Project> projectsMatchingName, String name);

    List<IdRev> searchIdRevForProjectId(Integer projectId);
}
