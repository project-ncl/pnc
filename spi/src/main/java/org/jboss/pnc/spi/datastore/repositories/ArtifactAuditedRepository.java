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
