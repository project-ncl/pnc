/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.datastore.repositories;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.Project;

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
