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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.pnc.AbstractTest;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationSetRepository;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.UserRepository;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

@RunWith(Arquillian.class)
@Transactional
@Category(ContainerTest.class)
public class RSQLTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Inject
    private UserRepository userRepository;

    @Inject
    private BuildConfigurationSetRepository buildConfigurationSetRepository;

    @Inject
    private RSQLPredicateProducer rsqlPredicateProducer;

    @Inject
    private SortInfoProducer sortInfoProducer;

    @Inject
    private PageInfoProducer pageInfoProducer;

    private static final AtomicBoolean isInitialized = new AtomicBoolean();

    @Deployment
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEarWithTestDependencies();
        WebArchive war = enterpriseArchive.getAsType(WebArchive.class, AbstractTest.REST_WAR_PATH);
        war.addClass(RSQLTest.class);
        logger.info("Deployment archive: " + enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Before
    public void before() {
        if(!isInitialized.getAndSet(true)) {
            userRepository.save(User.Builder.newBuilder().username("Abacki").email("a@rh.com").build());
            userRepository.save(User.Builder.newBuilder().username("Babacki").email("b@rh.com").build());
            userRepository.save(User.Builder.newBuilder().username("Cabacki").email("c@rh.com").build());

            buildConfigurationSetRepository.save(BuildConfigurationSet.Builder.newBuilder().id(101).name("test-unassociated-build-group").build());
        }
    }

    @Test
    public void shouldSelectDemoUser() {
        // given
        String rsqlQuery = "username==demo-user";

        // when
        List<User> users = selectUsers(rsqlQuery);

        //then
        assertThat(users).hasSize(1);
    }

    @Test
    public void shouldNotSelectNotExistingUser() {
        // given
        String rsqlQuery = "username==not-existing";

        // when
        List<User> users = selectUsers(rsqlQuery);

        //then
        assertThat(users).isEmpty();
    }

    @Test
    public void shouldSelectDemoUserWhenUserNameAndEmailIsProvided() {
        // given
        String rsqlQuery = "username==demo-user;email==demo-user@pnc.com";

        // when
        List<User> users = selectUsers(rsqlQuery);

        //then
        assertThat(users).hasSize(1);
    }

    @Test
    public void shouldNotSelectDemoUserWhenUserNameAndBadEmailIsProvided() {
        // given
        String rsqlQuery = "username==demo-user;email==bad-email@pnc.com";

        // when
        List<User> users = selectUsers(rsqlQuery);

        //then
        assertThat(users).isEmpty();
    }

    @Test
    public void shouldLimitReturnedUsers() {
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
    public void shouldSortById() {
        // given
        int pageSize = 999;
        int pageNumber = 0;
        String sortingQuery = "=asc=id";

        // when
        List<User> users = sortUsers(pageSize, pageNumber, sortingQuery);
        List<String> sortedUsers = nullableStreamOf(users).map(user -> user.getUsername()).collect(Collectors.toList());

        //then
        assertThat(sortedUsers).containsExactly("demo-user", "pnc-admin", "Abacki", "Babacki", "Cabacki");
    }

    @Test
    public void shouldFilterUsersBasedOnLikeOperator() {
        String[] queries = new String[] {
                "username=like=%aba%",
                "username=like=%Aba%",
                "username=like=aba%",
                "username=like=%babac%",
                "username=like=%cab%",
                "username=like=_abacki"
        };
        String[][] results = new String[][] { // must be sorted lexicographically
                {"Abacki", "Babacki", "Cabacki"},
                {"Abacki", "Babacki", "Cabacki"},
                {"Abacki"},
                {"Babacki"},
                {"Cabacki"},
                {"Babacki", "Cabacki"},
        };
        IntStream.range(0, queries.length)
                .forEach(i -> assertThat(
                        selectUsers(queries[i]).stream()
                                .map(User::getUsername)
                                .sorted(String::compareTo)
                                .collect(Collectors.toList())
                ).containsExactly(results[i]));
    }

    @Test
    public void shouldFilterBuildConfigurationSetsWithoutAssociatedProductVersion() {
        // given
        String rsqlQuery = "productVersion=isnull=true";

        // when
        List<BuildConfigurationSet> results = selectBuildConfigurationSets(rsqlQuery);

        // then
        assertThat(results).hasSize(1);
    }

    @Test
    public void shouldFilterBuildConfigurationSetsWithAssociatedProductVersion() {
        // given
        String rsqlQuery = "productVersion=isnull=false";

        // when
        List<BuildConfigurationSet> results = selectBuildConfigurationSets(rsqlQuery);

        // then
        assertThat(results).hasSize(2);
    }

    @Test
    public void shouldFilterUsingInOperator() {
        // given
        String rsqlQuery = "id=in=(1,2,1000,2000)";

        // when
        List<User> results = selectUsers(rsqlQuery);

        // then
        assertThat(results).hasSize(2);
    }

    @Test
    public void shouldFilterUsingOutOperator() {
        // given
        String rsqlQuery = "id=out=(2,3,4,5,6,7,8,9,10)";

        // when
        List<User> results = selectUsers(rsqlQuery);

        // then
        assertThat(results).hasSize(1);
    }

    private List<BuildConfigurationSet> selectBuildConfigurationSets(String rsqlQuery) {
        Predicate<BuildConfigurationSet> rsqlPredicate = rsqlPredicateProducer.getPredicate(BuildConfigurationSet.class, rsqlQuery);
        return nullableStreamOf(buildConfigurationSetRepository.queryWithPredicates(rsqlPredicate)).collect(Collectors.toList());
    }

    private List<User> selectUsers(String rsqlQuery) {
        Predicate<User> rsqlPredicate = rsqlPredicateProducer.getPredicate(User.class, rsqlQuery);
        return nullableStreamOf(userRepository.queryWithPredicates(rsqlPredicate)).collect(Collectors.toList());
    }

    private List<User> sortUsers(int pageSize, int offset, String sorting) {
        PageInfo pageInfo = pageInfoProducer.getPageInfo(offset, pageSize);
        SortInfo sortInfo = sortInfoProducer.getSortInfo(sorting);
        return nullableStreamOf(userRepository.queryAll(pageInfo, sortInfo)).collect(Collectors.toList());
    }
}
