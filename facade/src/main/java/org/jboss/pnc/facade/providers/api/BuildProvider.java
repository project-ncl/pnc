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

import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildRef;
import org.jboss.pnc.dto.response.Graph;
import org.jboss.pnc.dto.response.Page;

public interface BuildProvider extends Provider<org.jboss.pnc.model.BuildRecord, Build, BuildRef> {

    Page<Build> getPerformedBuildsForMilestone(int pageIndex,
                                               int pageSize,
                                               String sortingRsql,
                                               String query,
                                               Integer milestoneId);

    Page<Build> getBuildsForProject(int pageIndex,
                                    int pageSize,
                                    String sortingRsql,
                                    String query,
                                    Integer projectId);

    Page<Build> getBuildsForBuildConfiguration(int pageIndex,
                                               int pageSize,
                                               String sortingRsql,
                                               String query,
                                               Integer buildConfigurationId);
 
    Page<Build> getBuildsForUser(int pageIndex,
                                 int pageSize,
                                 String sortingRsql,
                                 String query,
                                 Integer userId);

    Page<Build> getBuildsForGroupConfiguration(BuildPageInfo pageInfo, int groupConfigurationId);

    Page<Build> getBuildsForGroupBuild(BuildPageInfo pageInfo, int groupBuildId);

    Graph<Build> getGroupBuildGraph(int id);
}
