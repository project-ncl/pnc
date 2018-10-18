/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.spi.repositorymanager;

import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.model.TargetRepository;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.jboss.pnc.spi.repositorymanager.model.RunningRepositoryDeletion;
import org.jboss.pnc.spi.repositorymanager.model.RunningRepositoryPromotion;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
public interface RepositoryManager {

    /**
     * Create a new repository session tuned to the parameters of that build collection and the build that will use this
     * repository session.
     *
     * @param buildExecution The build execution currently running
     * @param accessToken The access token to use
     * @param serviceAccountToken The access token for service account to use for repo creation, promotion and cleanup
     * @param repositoryType the created repositories' type (npm, maven, etc.)
     * @return The new repository session
     * @throws RepositoryManagerException If there is a problem creating the repository
     */
    RepositorySession createBuildRepository(BuildExecution buildExecution, String accessToken,
            String serviceAccountToken, RepositoryType repositoryType) throws RepositoryManagerException;

    /**
     * Add the repository containing output associated with the specified {@link BuildRecord} to the membership of the
     * repository group with the given ID.
     * Note that the operation won't start until monitoring starts for the returned {@link RunningRepositoryPromotion} instance.
     *
     * @param buildRecord The build output to promote
     * @param pakageType package type key used by repository manager
     * @param toGroup The ID of the repository group where the build output should be promoted
     * @param accessToken The access token to use
     * @return An object representing the running promotion process, with callbacks for result and error.
     *
     * @throws RepositoryManagerException If there is a problem promoting the build
     */
    RunningRepositoryPromotion promoteBuild(BuildRecord buildRecord, String pakageType, String toGroup,
            String accessToken) throws RepositoryManagerException;

    /**
     * Used to purge the artifacts that were output from a given build (including the specific hosted repository which was used
     * for that build).
     * Note that the operation won't start until monitoring starts for the returned {@link RunningRepositoryDeletion} instance.
     *
     * @param buildRecord The build whose artifacts/repositories should be removed
     * @param pakageType package type key used by repository manager
     * @param accessToken The access token to use
     * @return An object representing the running deletion, with callbacks for result and error.
     *
     * @throws RepositoryManagerException If there is a problem deleting the build
     */
    RunningRepositoryDeletion deleteBuild(BuildRecord buildRecord, String pakageType, String accessToken)
            throws RepositoryManagerException;

    /**
     * Closes connection to the repository driver for the given accessToken.
     * @param accessToken The access token to use
     */
    void close(String accessToken);

    boolean canManage(RepositoryType managerType);

}
