/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.datastore.repositories;

import org.assertj.core.api.Assertions;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.datastore.DeploymentFactory;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.datastore.Datastore;
import org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates;
import org.jboss.pnc.spi.datastore.predicates.UserPredicates;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.GraphWithMetadata;
import org.jboss.pnc.spi.datastore.repositories.UserRepository;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.util.graph.Vertex;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Jakub Bartecek
 */
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class BuildRecordRepositoryTest {

    private static final Logger logger = LoggerFactory.getLogger(BuildRecordRepositoryTest.class);

    private User user = null;

    @Inject
    private UserRepository userRepository;

    @Inject
    private BuildRecordRepository buildRecordRepository;

    @Inject
    private BuildConfigurationRepository buildConfigurationRepository;

    @Inject
    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;

    @Inject
    private Datastore datastore;

    @Inject
    Producers producers;

    @Deployment
    public static Archive<?> getDeployment() {
        return DeploymentFactory.createDatastoreDeployment();
    }

    @InSequence(1)
    @Test
    public void shouldFindNoneExpiredTemporaryBuilds() {
        // given
        Date now = new Date();
        BuildRecord givenBr = initBuildRecordBuilder(datastore.getNextBuildRecordId()).endTime(now)
                .temporaryBuild(true)
                .build();
        buildRecordRepository.save(givenBr);

        // when
        List<BuildRecord> found = buildRecordRepository
                .findIndependentTemporaryBuildsOlderThan(new Date(now.getTime() - 1000));

        // then
        assertEquals(0, found.size());
    }

    @InSequence(2)
    @Test
    public void shouldFindExpiredTemporaryBuilds() {
        // given
        BuildRecord givenBr = initBuildRecordBuilder(datastore.getNextBuildRecordId()).endTime(new Date(0))
                .temporaryBuild(true)
                .build();
        givenBr = buildRecordRepository.save(givenBr);

        // when
        List<BuildRecord> found = buildRecordRepository.findIndependentTemporaryBuildsOlderThan(new Date(1000));

        // then
        assertEquals(1, found.size());
        assertEquals(givenBr.getId(), found.get(0).getId());
    }

    @InSequence(3)
    @Test
    public void dependencyGraphTest() {
        // given
        Date now = new Date();
        BuildRecord buildRecord0 = initBuildRecordBuilder(100000).endTime(now)
                .temporaryBuild(true)
                .dependencyBuildRecordIds(new Integer[] { 100002 })
                .dependentBuildRecordIds(new Integer[] {})
                .build();
        buildRecordRepository.save(buildRecord0);

        BuildRecord buildRecord1 = initBuildRecordBuilder(100001).endTime(now)
                .temporaryBuild(true)
                .dependencyBuildRecordIds(new Integer[] { 100002 })
                .dependentBuildRecordIds(new Integer[] {})
                .build();
        buildRecordRepository.save(buildRecord1);

        BuildRecord buildRecord2 = initBuildRecordBuilder(100002).endTime(now)
                .temporaryBuild(true)
                .dependencyBuildRecordIds(new Integer[] { 100003, 100005, 110000, 100006 }) // missing record: 110000
                .dependentBuildRecordIds(new Integer[] { 100000, 100001 })
                .build();
        buildRecordRepository.save(buildRecord2);

        BuildRecord buildRecord3 = initBuildRecordBuilder(100003).endTime(now)
                .temporaryBuild(true)
                .dependencyBuildRecordIds(new Integer[] { 100004 })
                .dependentBuildRecordIds(new Integer[] { 100002 })
                .build();
        buildRecordRepository.save(buildRecord3);

        BuildRecord buildRecord4 = initBuildRecordBuilder(100004).endTime(now)
                .temporaryBuild(true)
                .dependencyBuildRecordIds(new Integer[] {})
                .dependentBuildRecordIds(new Integer[] { 100003 })
                .build();
        buildRecordRepository.save(buildRecord4);

        BuildRecord buildRecord5 = initBuildRecordBuilder(100005).endTime(now)
                .temporaryBuild(true)
                .dependencyBuildRecordIds(new Integer[] {})
                .dependentBuildRecordIds(new Integer[] { 100002 })
                .build();
        buildRecordRepository.save(buildRecord5);

        BuildRecord buildRecord6 = initBuildRecordBuilder(100006).endTime(now)
                .temporaryBuild(true)
                .dependencyBuildRecordIds(new Integer[] {})
                .dependentBuildRecordIds(new Integer[] { 100002 })
                .build();
        buildRecordRepository.save(buildRecord6);

        // when
        GraphWithMetadata<BuildRecord, Integer> dependencyGraph = buildRecordRepository.getDependencyGraph(100002);

        // then
        logger.info("Graph: {}", dependencyGraph.getGraph().toString());
        assertEquals(7, dependencyGraph.getGraph().size());

        Vertex<BuildRecord> vertex2 = dependencyGraph.getGraph().findVertexByName(100002 + "");
        BuildRecord buildRecord = vertex2.getData();
        assertNotNull(buildRecord.getBuildConfigurationAudited().getName());
        assertEquals(3, vertex2.getOutgoingEdgeCount());
        assertEquals(2, vertex2.getIncomingEdgeCount());

        Vertex<BuildRecord> vertex3 = dependencyGraph.getGraph().findVertexByName(100003 + "");
        assertEquals(1, vertex3.getOutgoingEdgeCount());
        assertEquals(1, vertex3.getIncomingEdgeCount());

        assertEquals(1, dependencyGraph.getMissingNodeIds().size());
        assertEquals("110000", dependencyGraph.getMissingNodeIds().get(0) + "");
    }

    @InSequence(4)
    @Test
    public void shouldGetRecordsWithoutAttributeKey() {
        // given
        Date now = new Date();
        BuildRecord buildRecord0 = initBuildRecordBuilder(200000).endTime(now)
                .temporaryBuild(true)
                .attribute("ATTR1", "X")
                .attribute("TEST", "true") // exclude all other builds
                .build();
        buildRecordRepository.save(buildRecord0);

        BuildRecord buildRecord1 = initBuildRecordBuilder(200001).endTime(now)
                .temporaryBuild(true)
                .attribute("ATTR1", "X")
                .attribute("ATTR2", "X")
                .attribute("TEST", "true")
                .build();
        buildRecordRepository.save(buildRecord1);

        // when
        List<BuildRecord> result = buildRecordRepository.queryWithPredicates(
                BuildRecordPredicates.withoutAttribute("ATTR2"),
                BuildRecordPredicates.withAttribute("TEST", "true"));

        // then
        logger.debug("Builds {}", result);
        Assertions.assertThat(result.size()).isEqualTo(1);
    }

    private BuildRecord.Builder initBuildRecordBuilder(Integer id) {
        if (user == null) {
            List<User> users = userRepository.queryWithPredicates(UserPredicates.withUserName("demo-user"));
            if (users.size() > 0) {
                user = users.get(0);
            }
            if (user == null) {
                this.user = userRepository.save(
                        User.Builder.newBuilder()
                                .username("demo-user")
                                .firstName("Demo First Name")
                                .lastName("Demo Last Name")
                                .email("demo-user@pnc.com")
                                .build());
            }
        }

        BuildConfiguration buildConfiguration = producers.createValidBuildConfiguration("buildRecordTest-" + id);
        BuildConfiguration saved = buildConfigurationRepository.save(buildConfiguration);
        BuildConfigurationAudited buildConfigurationAudited = buildConfigurationAuditedRepository
                .findAllByIdOrderByRevDesc(saved.getId())
                .get(0);

        return BuildRecord.Builder.newBuilder()
                .id(id)
                .buildConfigurationAudited(buildConfigurationAudited)
                .submitTime(new Date())
                .user(user)
                .status(BuildStatus.SUCCESS);
    }

}
