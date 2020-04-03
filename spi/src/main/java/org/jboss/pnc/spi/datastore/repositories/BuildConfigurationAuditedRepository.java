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

    BuildConfigurationAudited queryById(IdRev idRev);

    Map<IdRev, BuildConfigurationAudited> queryById(Set<IdRev> idRev);

    List<BuildConfigurationAudited> searchForBuildConfigurationName(String buildConfigurationName);

    List<IdRev> searchIdRevForBuildConfigurationName(String buildConfigurationName);

    List<IdRev> searchIdRevForBuildConfigurationNameOrProjectName(List<Project> projectsMatchingName, String name);

    List<IdRev> searchIdRevForProjectId(Integer projectId);

}
