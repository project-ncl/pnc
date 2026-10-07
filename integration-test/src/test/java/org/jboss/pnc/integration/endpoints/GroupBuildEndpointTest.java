/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.integration.endpoints;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Iterator;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.client.ClientException;
import org.jboss.pnc.client.GroupBuildClient;
import org.jboss.pnc.client.RemoteCollection;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.dto.GroupBuild;
import org.jboss.pnc.integration.setup.Deployments;
import org.jboss.pnc.integration.setup.RestClientConfiguration;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class GroupBuildEndpointTest {

    private static final Logger logger = LoggerFactory.getLogger(GroupBuildEndpointTest.class);

    private static String groupBuildId1;
    private static GroupBuildClient anonymousClient = new GroupBuildClient(RestClientConfiguration.asAnonymous());

    @Deployment
    public static EnterpriseArchive deploy() {
        return Deployments.testEar();
    }

    @BeforeClass
    public static void prepareData() throws Exception {

        Iterator<GroupBuild> it = anonymousClient.getAll().iterator();

        groupBuildId1 = it.next().getId();
    }

    @Test
    public void shouldGetGroupBuilds() throws RemoteResourceException {
        RemoteCollection<GroupBuild> all = anonymousClient.getAll();

        assertThat(all).hasSize(3);
    }

    @Test
    public void shouldGetSpecificGroupBuild() throws ClientException {

        GroupBuild build = anonymousClient.getSpecific(groupBuildId1);

        assertThat(build.getId()).isEqualTo(groupBuildId1);
        assertThat(build.getGroupConfig()).isNotNull();
        assertThat(build.getUser()).isNotNull();
        assertThat(build.getStartTime()).isNotNull();
        assertThat(build.getEndTime()).isNotNull();
        assertThat(build.getStatus()).isNotNull();
        assertThat(build.getTemporaryBuild()).isNotNull();
        assertThat(build.getGroupConfig()).isNotNull();
    }
}
