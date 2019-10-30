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
package org.jboss.pnc.spi.repositorymanager.model;

import org.jboss.pnc.model.TargetRepository;
import org.jboss.pnc.spi.executor.BuildExecutionSession;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerException;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
public interface RepositorySession {

    TargetRepository.Type getType();

    String getBuildRepositoryId();

    RepositoryConnectionInfo getConnectionInfo();

    /**
     * Process any uncaptured imports of input artifacts (dependencies, etc.) and return the result containing dependencies and
     * build output.
     *
     * @param session the session used to provide additional updates to the user, can be null
     * @param promote flag if promotion for the collected artifacts and dependencies should be done
     * @return The result of extracting the build artifacts
     * @throws RepositoryManagerException if there is a problem extracting build artifacts
     */
    RepositoryManagerResult extractBuildArtifacts(BuildExecutionSession session, boolean promote) throws RepositoryManagerException;

    /**
     * Removes build aggregation group. This should be done for every build.
     *
     * @throws RepositoryManagerException in case of an error received from repository manager
     */
    void deleteBuildGroup() throws RepositoryManagerException;

    void close();
}
