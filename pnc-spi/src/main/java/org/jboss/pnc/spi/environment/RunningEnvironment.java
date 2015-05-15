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

import java.io.InputStream;
import java.io.Serializable;

import org.jboss.pnc.spi.environment.exception.EnvironmentDriverException;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;

/**
 * Identification of environment started by environment driver
 * 
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
public interface RunningEnvironment extends Serializable, DestroyableEnvironmnet {

    /**
     * Transfers data to the running environment. The data are saved to the file on path specified
     * as parameter.
     * 
     * @param pathOnHost Path in the target environment, where the data are passed
     * @param stream Data, which will be transfered to the target container
     * @throws EnvironmentDriverException Thrown if it the data transfer couldn't be finished.
     */
    void transferDataToEnvironment(String pathOnHost, InputStream stream) throws EnvironmentDriverException;

    /**
     * Transfers data to the running environment. The data are saved to the file on path specified
     * as parameter.
     * 
     * @param pathOnHost Path in the target environment, where the data are passed
     * @param data Data, which will be transfered to the target container
     * @throws EnvironmentDriverException Thrown if it the data transfer couldn't be finished.
     */
    void transferDataToEnvironment(String pathOnHost, String data) throws EnvironmentDriverException;

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

}
