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

import org.jboss.pnc.model.Environment;
import org.jboss.pnc.spi.environment.exception.EnvironmentDriverException;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;

/**
 * SPI interface for Environment driver, which provides support
 * to control different target environments.
 * 
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
public interface EnvironmentDriver {

    /**
     * Creates and starts new clean environment.
     * 
     * @param buildEnvironment Specification of requested environment
     * @param repositorySession Configuration of repository to store built artifacts
     * 
     * @return New started environment in initialization phase
     * @throws EnvironmentDriverException Thrown if any error occurs during starting new environment
     */
    StartedEnvironment buildEnvironment(Environment buildEnvironment,
            RepositorySession repositorySession) throws EnvironmentDriverException;

    /**
     * Test if selected driver can build requested environment
     * 
     * @param environment Specification of requested environment
     * @return True, if selected driver can build requested environment, otherwise false.
     */
    boolean canBuildEnvironment(Environment environment);

}
