/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.datastore.repositories;

import java.util.List;

import org.jboss.pnc.model.ArtifactAudited;
import org.jboss.pnc.model.IdRev;

/**
 * Interface for manipulating {@link org.jboss.pnc.model.ArtifactAudited} entity.
 */
public interface ArtifactAuditedRepository {
    List<ArtifactAudited> findAllByIdOrderByRevDesc(Integer id);

    /**
     * Lookups a ArtifactAudited entity
     *
     * @param idRev Id and Revision of a desired ArtifactAudited entity
     * @return ArtifactAudited or null if there is no such entity
     */
    ArtifactAudited queryById(IdRev idRev);

    /**
     * Finds latest revision of an artifact with given ID.
     * 
     * @param artifactId ID of the Artifact.
     * @return Latest audited revision of the Artifact or null if the Artifact does not exists.
     */
    ArtifactAudited findLatestById(int artifactId);

}
