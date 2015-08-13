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

import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.spi.BuildExecution;
import org.jboss.pnc.spi.builddriver.CompletedBuild;
import org.jboss.pnc.spi.builddriver.RunningBuild;
import org.jboss.pnc.test.category.RemoteTest;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@Category(RemoteTest.class)
public class TermdBuildDriverRemoteTest extends AbstractLocalBuildAgentTest {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @BeforeClass
    public static void checkConnectionToGithub() throws Exception {
        URL githubUrl = new URL("http://github.com/");
        URLConnection githubConnection = null;
        try {
            githubConnection = githubUrl.openConnection();
            githubConnection.connect();
        } catch (MalformedURLException e) {
            fail("can't happen");
        } catch (IOException e) {
            fail("Unable to connect to github. A network error? " + e.getMessage());
        }
    }

    @Test(timeout = 60_000)
    public void shouldBuildJSR107() throws Exception {
        //given
        TermdBuildDriver driver = new TermdBuildDriver();
        BuildExecution buildExecution = mock(BuildExecution.class);

        AtomicReference<CompletedBuild> buildResult = new AtomicReference<>();

        BuildConfigurationAudited jsr107BuildConfig = mock(BuildConfigurationAudited.class);
        doReturn("https://github.com/jsr107/jsr107spec.git").when(jsr107BuildConfig).getScmRepoURL();
        doReturn("master").when(jsr107BuildConfig).getScmRevision();
        doReturn("mvn validate").when(jsr107BuildConfig).getBuildScript();
        doReturn("jsr107-test").when(jsr107BuildConfig).getName();

        //when
        RunningBuild runningBuild = driver.startProjectBuild(buildExecution, jsr107BuildConfig, localEnvironmentPointer);
        runningBuild.monitor(completedBuild -> buildResult.set(completedBuild), exception -> fail(exception.getMessage()));

        logger.info("==== shouldBuildJSR107 logs ====");
        logger.info(buildResult.get().getBuildResult().getBuildLog());
        logger.info("==== /shouldBuildJSR107 logs ====");

        //then
        assertThat(buildResult.get().getBuildResult()).isNotNull();
        assertThat(buildResult.get().getBuildResult().getBuildLog()).isNotEmpty();
        assertThat(Files.exists(localEnvironmentPointer.getWorkingDirectory())).isTrue();
        assertThat(Files.exists(localEnvironmentPointer.getWorkingDirectory().resolve(jsr107BuildConfig.getName()))).isTrue();

    }
}