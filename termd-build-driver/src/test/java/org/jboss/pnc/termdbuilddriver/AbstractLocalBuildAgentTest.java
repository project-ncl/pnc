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
package org.jboss.pnc.termdbuilddriver;

import org.jboss.pnc.buildagent.server.BuildAgent;
import org.jboss.pnc.spi.builddriver.DebugData;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AbstractLocalBuildAgentTest {

    protected static BuildAgent localBuildAgent;
    private static Path workingDirectory;
    protected RunningEnvironment localEnvironmentPointer;

    protected URI baseBuildAgentUri;

    private static Logger log = LoggerFactory.getLogger(AbstractLocalBuildAgentTest.class);

    private org.jboss.pnc.spi.builddriver.DebugData debugData = new DebugData(false);

    @BeforeClass
    public static void beforeClass() throws Exception {
        workingDirectory = Files.createTempDirectory("termd-build-agent");
        workingDirectory.toFile().deleteOnExit();

        localBuildAgent = TermdServer.startServer("127.0.0.1", 0, "", Optional.of(workingDirectory));
    }

    @AfterClass
    public static void afterClass() throws IOException {
        TermdServer.stopServer();
    }

    @Before
    public void beforeAbstract() throws Exception {
        baseBuildAgentUri = new URI("http://" + localBuildAgent.getHost() + ":" + localBuildAgent.getPort() + "/");

        localEnvironmentPointer = mock(RunningEnvironment.class);
        when(localEnvironmentPointer.getId()).thenReturn("test");
        when(localEnvironmentPointer.getBuildAgentPort()).thenReturn(localBuildAgent.getPort());
        when(localEnvironmentPointer.getBuildAgentUrl()).thenReturn(baseBuildAgentUri.toString());
        when(localEnvironmentPointer.getInternalBuildAgentUrl()).thenReturn(baseBuildAgentUri.toString());
        when(localEnvironmentPointer.getWorkingDirectory()).thenReturn(workingDirectory);
        when(localEnvironmentPointer.getDebugData()).thenReturn(debugData);
    }

    protected Path getWorkingDirectory() {
        return workingDirectory;
    }
}