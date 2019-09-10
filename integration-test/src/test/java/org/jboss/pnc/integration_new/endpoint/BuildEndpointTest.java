/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.integration_new.endpoint;

import org.assertj.core.api.Assertions;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.client.BuildClient;
import org.jboss.pnc.client.RemoteCollection;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.integration_new.setup.Deployments;
import org.jboss.pnc.integration_new.setup.RestClientConfiguration;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ForbiddenException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class BuildEndpointTest {

    private static final Logger logger = LoggerFactory.getLogger(BuildEndpointTest.class);

    @Deployment
    public static EnterpriseArchive deploy() {
        return Deployments.testEar();
    }

    @Test
    public void shouldGetByStatusAndLog() throws RemoteResourceException {
        BuildClient client = new BuildClient(RestClientConfiguration.getConfiguration(RestClientConfiguration.AuthenticateAs.NONE));
        RemoteCollection<Build> builds = client.getAllByStatusAndLogContaining(BuildStatus.SUCCESS, "fox");
        Assertions.assertThat(builds.size()).isGreaterThan(0);
        Build build = builds.iterator().next();
        logger.info("Found build:" + build.toString());
    }

    @Test
    public void shouldSetBuiltArtifacts() throws RemoteResourceException {
        BuildClient client = new BuildClient(RestClientConfiguration.getConfiguration(RestClientConfiguration.AuthenticateAs.SYSTEM_USER));

        String buildRecordId = "1";
        RemoteCollection<Artifact> artifacts = client.getBuiltArtifacts(buildRecordId);
        Set<Integer> artifactIds = artifactIds(artifacts);
        Assertions.assertThat(artifactIds).contains(100, 101);

        client.setBuiltArtifacts(buildRecordId, Collections.singletonList("101"));
        RemoteCollection<Artifact> newBuiltArtifacts = client.getBuiltArtifacts(buildRecordId);
        Set<Integer> updatedArtifactIds = artifactIds(newBuiltArtifacts);
        Assertions.assertThat(updatedArtifactIds).contains(101);
    }

    @Test
    public void shouldSetDependentArtifacts() throws RemoteResourceException {
        BuildClient client = new BuildClient(RestClientConfiguration.getConfiguration(RestClientConfiguration.AuthenticateAs.SYSTEM_USER));

        String buildRecordId = "1";
        RemoteCollection<Artifact> artifacts = client.getDependencyArtifacts(buildRecordId);
        Set<Integer> artifactIds = artifactIds(artifacts);
        Assertions.assertThat(artifactIds).contains(102, 103);

        client.setDependentArtifacts(buildRecordId, Collections.singletonList("102"));
        RemoteCollection<Artifact> newDependencyArtifacts = client.getDependencyArtifacts(buildRecordId);
        Set<Integer> updatedArtifactIds = artifactIds(newDependencyArtifacts);
        Assertions.assertThat(updatedArtifactIds).contains(102);
    }

    @Test
    public void shouldFailAsRegularUser() {
        BuildClient client = new BuildClient(RestClientConfiguration.getConfiguration(RestClientConfiguration.AuthenticateAs.USER));

        String buildRecordId = "1";
        Exception caught = null;
        try {
            client.setBuiltArtifacts(buildRecordId, Collections.emptyList());
        } catch (RemoteResourceException e) {
            caught = e;
        }
        Assertions.assertThat(caught.getCause()).isInstanceOf(ForbiddenException.class);

        caught = null;
        try {
            client.setDependentArtifacts(buildRecordId, Collections.emptyList());
        } catch (RemoteResourceException e) {
            caught = e;
        }
        Assertions.assertThat(caught.getCause()).isInstanceOf(ForbiddenException.class);

        caught = null;
        try {
            client.delete(buildRecordId);
        } catch (RemoteResourceException e) {
            caught = e;
        }
        Assertions.assertThat(caught.getCause()).isInstanceOf(ForbiddenException.class);

        caught = null;
        try {
            client.update(buildRecordId, Build.builder().build());
        } catch (RemoteResourceException e) {
            caught = e;
        }
        Assertions.assertThat(caught.getCause()).isInstanceOf(ForbiddenException.class);
    }

    private Set<Integer> artifactIds(RemoteCollection<Artifact> artifacts) {
        Set<Integer> artifactIds = new HashSet<>();
        for (Artifact a : artifacts) {
            artifactIds.add(Integer.valueOf(a.getId()));
        }
        return artifactIds;
    }

}
