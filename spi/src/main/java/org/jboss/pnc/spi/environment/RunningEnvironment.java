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
package org.jboss.pnc.spi.environment;

import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;

import java.io.Serializable;
import java.nio.file.Path;

/**
 * Identification of environment started by environment driver
 * 
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
public interface RunningEnvironment extends Serializable, DestroyableEnvironment {

    /**
     * 
     * @return ID of an environment
     */
    String getId();

    /**
     * 
     * @return Port to connect to Jenkins UI
     */
    int getJenkinsPort();

    /**
     * @return Jenkins URL in format IP:PORT
     */
    String getJenkinsUrl();

    /**
     * @return Repository configuration related to the running environment
     */
    RepositorySession getRepositorySession();

    /**
     * @return Returns a build directory.
     */
    Path getWorkingDirectory();

}
