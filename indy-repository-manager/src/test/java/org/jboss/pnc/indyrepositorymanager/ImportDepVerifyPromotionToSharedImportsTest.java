/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.indyrepositorymanager;

import org.commonjava.indy.client.core.util.UrlUtils;
import org.commonjava.indy.model.core.StoreKey;
import org.commonjava.indy.model.core.StoreType;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.indyrepositorymanager.fixture.TestBuildExecution;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.spi.repositorymanager.BuildExecution;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.jboss.pnc.test.category.ContainerTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static org.commonjava.indy.pkg.maven.model.MavenPackageTypeDescriptor.MAVEN_PKG_KEY;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jboss.pnc.indyrepositorymanager.IndyRepositoryConstants.SHARED_IMPORTS_ID;

@Category(ContainerTest.class)
public class ImportDepVerifyPromotionToSharedImportsTest extends AbstractImportTest {

    @Test
    public void extractBuildArtifactsTriggersImportPromotion() throws Exception {
        String path = "/org/myproj/myproj/1.0/myproj-1.0.pom";
        String content = "This is a test " + System.currentTimeMillis();

        // setup the expectation that the remote repo pointing at this server will request this file...and define its
        // content.
        server.expect(server.formatUrl(STORE, path), 200, content);

        // create a dummy non-chained build execution and repo session based on it
        BuildExecution execution = new TestBuildExecution();

        RepositorySession session = driver.createBuildRepository(
                execution,
                accessToken,
                accessToken,
                RepositoryType.MAVEN,
                Collections.emptyMap(),
                false);

        // simulate a build resolving an artifact via the Indy remote repository.
        assertThat(download(UrlUtils.buildUrl(session.getConnectionInfo().getDependencyUrl(), path)), equalTo(content));

        // now, extract the build artifacts. This will trigger promotion of imported stuff to shared-imports.
        RepositoryManagerResult result = session.extractBuildArtifacts(true);

        // do some sanity checks while we're here
        List<Artifact> deps = result.getDependencies();
        assertThat(deps.size(), equalTo(1));

        Artifact a = deps.get(0);
        assertThat(a.getFilename(), equalTo(new File(path).getName()));

        // end result: you should be able to download this artifact from shared-imports now.
        StoreKey sharedImportsKey = new StoreKey(MAVEN_PKG_KEY, StoreType.hosted, SHARED_IMPORTS_ID);
        assertThat(download(indy.content().contentUrl(sharedImportsKey, path)), equalTo(content));
    }

}
