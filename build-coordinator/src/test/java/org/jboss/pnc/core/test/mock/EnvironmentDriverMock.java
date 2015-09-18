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
package org.jboss.pnc.core.test.mock;

import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.spi.environment.EnvironmentDriver;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.jboss.pnc.spi.environment.StartedEnvironment;
import org.jboss.pnc.spi.environment.exception.EnvironmentDriverException;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;

import javax.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * 
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
@ApplicationScoped
public class EnvironmentDriverMock implements EnvironmentDriver {

    @Override
    public StartedEnvironment buildEnvironment(BuildEnvironment buildSystemImage,
            final RepositorySession repositoryConfiguration) throws EnvironmentDriverException {
        return new StartedEnvironment() {

            @Override
            public void destroyEnvironment() throws EnvironmentDriverException {

            }

            @Override
            public void monitorInitialization(Consumer<RunningEnvironment> onComplete,
                    Consumer<Exception> onError) {
                onComplete.accept(
                        new RunningEnvironment() {

                            @Override
                            public RepositorySession getRepositorySession() {
                                return repositoryConfiguration;
                            }

                            @Override
                            public Path getWorkingDirectory() {
                                try {
                                    Path tempDirectory = Files.createTempDirectory("EnvironmentDriverMock");
                                    tempDirectory.toFile().deleteOnExit();
                                    return tempDirectory;
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }

                            @Override
                            public String getJenkinsUrl() {
                                return "http://10.10.10.10:8080";
                            }

                            @Override
                            public int getJenkinsPort() {
                                return 0;
                            }

                            @Override
                            public String getId() {
                                return null;
                            }

                            @Override
                            public void destroyEnvironment() throws EnvironmentDriverException {
                            }
                        });
            }

            @Override
            public String getId() {
                return null;
            }
        };

    }

    @Override
    public boolean canBuildEnvironment(BuildEnvironment buildSystemImage) {
        return true;
    }

}
