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
package org.jboss.pnc.datastore.repositories;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.datastore.DeploymentFactory;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.datastore.Datastore;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.UserRepository;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

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

    private User user = null;

    @Inject
    private UserRepository userRepository;

    @Inject
    private BuildRecordRepository buildRecordRepository;

    @Inject
    private Datastore datastore;

    @Deployment
    public static Archive<?> getDeployment() {
        return DeploymentFactory.createDatastoreDeployment();
    }

    @InSequence(1)
    @Test
    public void shouldFindNoneExpiredTemporaryBuilds() {
        // given
        Date now = new Date();
        BuildRecord givenBr = initBuildRecordBuilder()
                .endTime(now)
                .temporaryBuild(true)
                .build();
        buildRecordRepository.save(givenBr);

        // when
        List<BuildRecord> found = buildRecordRepository.findTemporaryBuildsOlderThan(new Date(now.getTime() - 1000));

        // then
        assertEquals(0, found.size());
    }

    @InSequence(2)
    @Test
    public void shouldFindExpiredTemporaryBuilds() {
        // given
        BuildRecord givenBr = initBuildRecordBuilder()
                .endTime(new Date(0))
                .temporaryBuild(true)
                .build();
        givenBr = buildRecordRepository.save(givenBr);

        // when
        List<BuildRecord> found = buildRecordRepository.findTemporaryBuildsOlderThan(new Date(1000));

        // then
        assertEquals(1, found.size());
        assertEquals(givenBr.getId(), found.get(0).getId());
    }


    private BuildRecord.Builder initBuildRecordBuilder() {
        if(user == null) {
            user = userRepository.save(User.Builder.newBuilder()
                    .id(1)
                    .username("demo-user")
                    .firstName("Demo First Name")
                    .lastName("Demo Last Name")
                    .email("demo-user@pnc.com")
                    .build());
        }

        return BuildRecord.Builder.newBuilder()
                .id(datastore.getNextBuildRecordId())
                .buildConfigurationAuditedId(1)
                .buildConfigurationAuditedRev(1)
                .submitTime(new Date())
                .user(User.Builder.newBuilder().id(1).build())
                .status(BuildStatus.SUCCESS);
    }



}
