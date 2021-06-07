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
import org.jboss.pnc.common.concurrent.Sequence;
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
import org.jboss.pnc.spi.datastore.repositories.UserRepository;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

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
        BuildRecord givenBr = initBuildRecordBuilder(Sequence.nextBase32Id()).endTime(now).temporaryBuild(true).build();
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
        BuildRecord givenBr = initBuildRecordBuilder(Sequence.nextBase32Id()).endTime(new Date(0))
                .temporaryBuild(true)
                .build();
        givenBr = buildRecordRepository.save(givenBr);

        // when
        List<BuildRecord> found = buildRecordRepository.findIndependentTemporaryBuildsOlderThan(new Date(1000));

        // then
        assertEquals(1, found.size());
        assertEquals(givenBr.getId(), found.get(0).getId());
    }

    @InSequence(4)
    @Test
    public void shouldGetRecordsWithoutAttributeKey() {
        // given
        Date now = new Date();
        BuildRecord buildRecord0 = initBuildRecordBuilder("CERBB5D55GARK").endTime(now)
                .temporaryBuild(true)
                .attribute("ATTR1", "X")
                .attribute("TEST", "true") // exclude all other builds
                .build();
        buildRecordRepository.save(buildRecord0);

        BuildRecord buildRecord1 = initBuildRecordBuilder("200001").endTime(now)
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

    private BuildRecord.Builder initBuildRecordBuilder(String id) {
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
