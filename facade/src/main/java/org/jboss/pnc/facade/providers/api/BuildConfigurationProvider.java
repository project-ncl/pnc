/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.facade.providers.api;

import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.dto.BuildConfigurationRef;
import org.jboss.pnc.dto.BuildConfigurationRevision;
import org.jboss.pnc.dto.requests.BuildConfigWithSCMRequest;
import org.jboss.pnc.dto.response.BuildConfigCreationResponse;
import org.jboss.pnc.dto.response.Page;

import java.util.Optional;

public interface BuildConfigurationProvider extends Provider<org.jboss.pnc.model.BuildConfiguration, BuildConfiguration, BuildConfigurationRef> {

    Page<BuildConfiguration> getBuildConfigurationsForProductVersion(int pageIndex,
                                                                     int pageSize,
                                                                     String sortingRsql,
                                                                     String query,
                                                                     Integer productVersionId);

    Page<BuildConfiguration> getBuildConfigurationsForProject(int pageIndex,
                                                              int pageSize,
                                                              String sortingRsql,
                                                              String query,
                                                              Integer projectId);

    Page<BuildConfiguration> getBuildConfigurationsForGroup(int pageIndex,
                                                            int pageSize,
                                                            String sortingRsql,
                                                            String query,
                                                            int groupConfigId);

    BuildConfiguration clone(Integer buildConfigurationId);

    void addDependency(Integer configId, Integer dependencyId);

    void removeDependency(Integer configId, Integer dependencyId);

    Page<BuildConfiguration> getDependencies(int pageIndex, int pageSize, String sortingRsql, String query, Integer configId);


    Page<BuildConfigurationRevision> getRevisions(int pageIndex, int pageSize, Integer id);

    BuildConfigurationRevision getRevision(Integer id, Integer rev);

    BuildConfigurationRevision createRevision(int id, BuildConfiguration buildConfiguration);

    BuildConfigCreationResponse createWithScm(BuildConfigWithSCMRequest request);

    Optional<BuildConfiguration> restoreRevision(int id, int rev);
}
