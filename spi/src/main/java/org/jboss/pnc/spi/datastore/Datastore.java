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
package org.jboss.pnc.spi.datastore;

import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.User;

/**
 * Topmost datastore interface.
 */
public interface Datastore {

    /**
     * Stores a complete build.
     *
     * @param buildRecord Completed BuildRecord.
     * @throws DatastoreException Thrown if database is unable to process the request.
     */
    BuildRecord storeCompletedBuild(BuildRecord buildRecord) throws DatastoreException;

    /**
     * Returns User upon its username.
     *
     * @param username Username of the user.
     * @return User entity.
     */
    User retrieveUserByUsername(String username);

    /**
     * Creates new user.
     *
     * @param user User entity.
     */
    void createNewUser(User user);

    /**
     * Gets next generated Build Record Id.
     *
     * @return A generated Build Record Id.
     */
    int getNextBuildRecordId();

    /**
     * Save build config set record to db
     * 
     * @param buildConfigSetRecord
     * @return
     * @throws DatastoreException
     */
    BuildConfigSetRecord saveBuildConfigSetRecord(BuildConfigSetRecord buildConfigSetRecord) throws DatastoreException;

    /**
     * Get the latest audited version of the given build configuration.
     * 
     * @param buildConfigId The id of the config to check
     * @return The latest audited version of the build configuration
     */
    BuildConfigurationAudited getLatestBuildConfigurationAudited(Integer buildConfigId);
}
