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
package org.jboss.pnc.integration;

import cz.jirutka.rsql.parser.RSQLParserException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.pnc.datastore.limits.RSQLPageLimitAndSortingProducer;
import org.jboss.pnc.datastore.predicates.RSQLPredicateProducer;
import org.jboss.pnc.datastore.repositories.UserRepository;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.model.User;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;

import javax.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

@RunWith(Arquillian.class)
@Transactional
@Category(ContainerTest.class)
public class RSQLTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Inject
    private UserRepository userRepository;

    private static final AtomicBoolean isInitialized = new AtomicBoolean();

    @Deployment
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEarWithTestDependencies();
        WebArchive war = enterpriseArchive.getAsType(WebArchive.class, "/pnc-rest.war");
        war.addClass(RSQLTest.class);
        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Before
    public void before() {
        if(!isInitialized.getAndSet(true)) {
            userRepository.save(User.Builder.newBuilder().username("Abacki").email("a@rh.com").build());
            userRepository.save(User.Builder.newBuilder().username("Babacki").email("b@rh.com").build());
            userRepository.save(User.Builder.newBuilder().username("Cabacki").email("c@rh.com").build());
        }
    }

    @Test
    public void shouldSelectDemoUser() throws RSQLParserException {
        // given
        String rsqlQuery = "username==demo-user";

        // when
        List<User> users = selectUsers(rsqlQuery);

        //then
        assertThat(users).hasSize(1);
    }

    @Test
    public void shouldNotSelectNotExistingUser() throws RSQLParserException {
        // given
        String rsqlQuery = "username==not-existing";

        // when
        List<User> users = selectUsers(rsqlQuery);

        //then
        assertThat(users).isEmpty();
    }

    @Test
    public void shouldSelectDemoUserWhenUserNameAndEmailIsProvided() throws RSQLParserException {
        // given
        String rsqlQuery = "username==demo-user;email==demo-user@pnc.com";

        // when
        List<User> users = selectUsers(rsqlQuery);

        //then
        assertThat(users).hasSize(1);
    }

    @Test
    public void shouldNotSelectDemoUserWhenUserNameAndBadEmailIsProvided() throws RSQLParserException {
        // given
        String rsqlQuery = "username==demo-user;email==bad-email@pnc.com";

        // when
        List<User> users = selectUsers(rsqlQuery);

        //then
        assertThat(users).isEmpty();
    }

    @Test
    public void shouldLimitReturnedUsers() throws RSQLParserException {
        // given
        int pageSize = 1;
        int pageNumber = 0;
        String sortingQuery = "";

        // when
        List<User> users = sortUsers(pageSize, pageNumber, sortingQuery);

        //then
        assertThat(users).hasSize(1);
    }

    @Test
    public void shouldSortById() throws RSQLParserException {
        // given
        int pageSize = 999;
        int pageNumber = 0;
        String sortingQuery = "=asc=id";

        // when
        List<User> users = sortUsers(pageSize, pageNumber, sortingQuery);
        List<String> sortedUsers = nullableStreamOf(users).map(user -> user.getUsername()).collect(Collectors.toList());

        //then
        assertThat(sortedUsers).containsExactly("demo-user", "Abacki", "Babacki", "Cabacki");
    }

    private List<User> selectUsers(String rsqlQuery) throws RSQLParserException {
        return nullableStreamOf(userRepository.findAll(RSQLPredicateProducer.fromRSQL(User.class, rsqlQuery).get())).collect(Collectors.toList());
    }

    private List<User> sortUsers(int pageSize, int offset, String sorting) throws RSQLParserException {
        Pageable pageable = RSQLPageLimitAndSortingProducer.fromRSQL(pageSize, offset, sorting);
        return nullableStreamOf(userRepository.findAll(pageable)).collect(Collectors.toList());
    }
}
