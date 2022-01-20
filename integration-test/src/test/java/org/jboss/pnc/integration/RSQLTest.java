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
package org.jboss.pnc.integration;

import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.client.BuildClient;
import org.jboss.pnc.client.ClientException;
import org.jboss.pnc.client.RemoteCollection;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.integration.setup.Deployments;
import org.jboss.pnc.integration.setup.RestClientConfiguration;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import static java.util.Optional.empty;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.jboss.pnc.client.BuildConfigurationClient;
import org.jboss.pnc.client.ProjectClient;
import org.jboss.pnc.demo.data.DatabaseDataInitializer;
import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.dto.Project;
import static org.jboss.pnc.integration.setup.IntegrationTestEnv.getHttpPort;
import static org.jboss.pnc.integration.setup.RestClientConfiguration.BASE_REST_PATH;
import org.jboss.pnc.rest.api.swagger.response.SwaggerPages.BuildConfigPage;

/**
 * @author <a href="mailto:jbrazdil@redhat.com">Honza Brazdil</a>
 *
 * @see org.jboss.pnc.demo.data.DatabaseDataInitializer
 */
@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class RSQLTest {

    private static final Logger logger = LoggerFactory.getLogger(RSQLTest.class);

    private final BuildClient buildClient = new BuildClient(RestClientConfiguration.asAnonymous());
    private final BuildConfigurationClient buildConfigClient = new BuildConfigurationClient(
            RestClientConfiguration.asAnonymous());
    private final ProjectClient projectClient = new ProjectClient(RestClientConfiguration.asAnonymous());

    @Deployment
    public static EnterpriseArchive deploy() {
        return Deployments.testEar();
    }

    @Test
    public void shouldConvertBoolean() throws RemoteResourceException {
        String queryTemporary = "temporaryBuild==TRUE";
        RemoteCollection<Build> temporary = buildClient.getAll(null, null, empty(), Optional.of(queryTemporary));
        assertThat(temporary).hasSize(2);

        String queryPersistent = "temporaryBuild==FALSE";
        RemoteCollection<Build> persistent = buildClient.getAll(null, null, empty(), Optional.of(queryPersistent));
        assertThat(persistent).hasSize(2);
    }

    @Test
    public void shouldFailWithMisingSelectorElement() throws RemoteResourceException {
        String queryTemporary = "environment==foo";
        assertThatThrownBy(() -> buildClient.getAll(null, null, empty(), Optional.of(queryTemporary)))
                .isInstanceOf(ClientException.class);
    }

    @Test
    public void shouldConvertEnum() throws RemoteResourceException {
        String queryFailed = "status==FAILED";
        RemoteCollection<Build> temporary = buildClient.getAll(null, null, empty(), Optional.of(queryFailed));
        assertThat(temporary).isEmpty();

        String querySuccess = "status==SUCCESS";
        RemoteCollection<Build> persistent = buildClient.getAll(null, null, empty(), Optional.of(querySuccess));
        assertThat(persistent).hasSize(4);
    }

    @Test
    public void shouldConvertDate() throws RemoteResourceException {
        String queryLT = "endTime=lt=2019-01-01T00:00:00Z";
        RemoteCollection<Build> temporary = buildClient.getAll(null, null, empty(), Optional.of(queryLT));
        assertThat(temporary).isEmpty();

        String queryGT = "endTime=gt=2019-01-01T00:00:00Z";
        RemoteCollection<Build> persistent = buildClient.getAll(null, null, empty(), Optional.of(queryGT));
        assertThat(persistent).hasSize(4);
    }

    @Test
    public void shouldReturnTemporaryBuildsOlderThanTimestamp() throws RemoteResourceException {
        String queryLT = "endTime=lt=2019-02-02T00:00:00Z;temporaryBuild==TRUE";
        RemoteCollection<Build> temporary = buildClient.getAll(null, null, empty(), Optional.of(queryLT));
        assertThat(temporary).hasSize(1);
    }

    @Test
    public void shouldFailSortById() throws RemoteResourceException {
        String sortQuery = "sort=desc=id";
        assertThatThrownBy(() -> buildClient.getAll(null, null, Optional.of(sortQuery), empty()))
                .isInstanceOf(ClientException.class);
    }

    @Test
    public void shouldSelectBuildConfig() throws RemoteResourceException {
        String query = "name==" + DatabaseDataInitializer.PNC_PROJECT_BUILD_CFG_ID;
        RemoteCollection<BuildConfiguration> bcs = buildConfigClient.getAll(empty(), Optional.of(query));
        assertThat(bcs).hasSize(1);
    }

    @Test
    public void shouldNotSelectNonExistingBuildConfig() throws RemoteResourceException {
        String query = "name==ThisMustTrullyBeANonexistentBuildConfigName";
        RemoteCollection<BuildConfiguration> bcs = buildConfigClient.getAll(empty(), Optional.of(query));
        assertThat(bcs).isEmpty();
    }

    @Test
    public void shouldSelectBuildConfigWithAndCombindation() throws RemoteResourceException {
        String query = "name==" + DatabaseDataInitializer.PNC_PROJECT_BUILD_CFG_ID + ";scmRevision==\"*/v0.2\"";
        RemoteCollection<BuildConfiguration> bcs = buildConfigClient.getAll(empty(), Optional.of(query));
        assertThat(bcs).hasSize(1);
    }

    @Test
    public void shouldNotSelectBuildConfigWithNonExistingAndCombindation() throws RemoteResourceException {
        String query = "name==" + DatabaseDataInitializer.PNC_PROJECT_BUILD_CFG_ID + ";scmRevision==NonExistingRev";
        RemoteCollection<BuildConfiguration> bcs = buildConfigClient.getAll(empty(), Optional.of(query));
        assertThat(bcs).isEmpty();
    }

    @Test
    public void shouldSelectBuildConfigWithOrCombindation() throws RemoteResourceException {
        String query = "name==" + DatabaseDataInitializer.PNC_PROJECT_BUILD_CFG_ID + ",name==termd";
        RemoteCollection<BuildConfiguration> bcs = buildConfigClient.getAll(empty(), Optional.of(query));
        assertThat(bcs).hasSize(2);
    }

    @Test
    public void shouldSelectBuildConfigWithOneNonExistingOrCombindation() throws RemoteResourceException {
        String query = "name==ThisMustTrullyBeANonexistentBuildConfigName,name==termd";
        RemoteCollection<BuildConfiguration> bcs = buildConfigClient.getAll(empty(), Optional.of(query));
        assertThat(bcs).hasSize(1);
    }

    @Test
    public void shouldSortBuildConfigByName() throws RemoteResourceException {
        String sortQury = "sort=asc=name";
        RemoteCollection<BuildConfiguration> bcs = buildConfigClient.getAll(Optional.of(sortQury), empty());
        Collection<BuildConfiguration> all = bcs.getAll();
        assertThat(all).hasSizeGreaterThan(1); // sorting of 1 or 0 is pointles

        List<BuildConfiguration> sorted = all.stream()
                .sorted(Comparator.comparing(BuildConfiguration::getName))
                .collect(Collectors.toList());
        assertThat(all).containsExactlyElementsOf(sorted);
    }

    @Test
    public void shouldReverseSortBuildConfigByName() throws RemoteResourceException {
        String sortQury = "sort=desc=name";
        RemoteCollection<BuildConfiguration> bcs = buildConfigClient.getAll(Optional.of(sortQury), empty());
        Collection<BuildConfiguration> all = bcs.getAll();
        assertThat(all).hasSizeGreaterThan(1); // sorting of 1 or 0 is pointles

        List<BuildConfiguration> reverseSorted = all.stream()
                .sorted(Comparator.comparing(BuildConfiguration::getName).reversed())
                .collect(Collectors.toList());
        assertThat(all).containsExactlyElementsOf(reverseSorted);
    }

    @Test
    public void shouldFilterProjectsBasedOnLikeOperator() throws RemoteResourceException {
        String sortQury = "sort=asc=name";
        String[] queries = new String[] { "name=like=%De%", "name=like=%de%", "name=like=*de*", "name=like=P%",
                "name=like=P*", "name=like=%termd%", "name=like=_auseway", "name=like=?auseway" };
        String[][] results = new String[][] { // must be sorted lexicographically
                { "Dependency Analysis", "Project Newcastle Demo Project 1" },
                { "Dependency Analysis", "Project Newcastle Demo Project 1" },
                { "Dependency Analysis", "Project Newcastle Demo Project 1" },
                { "Pnc Build Agent", "Project Newcastle Demo Project 1" },
                { "Pnc Build Agent", "Project Newcastle Demo Project 1" }, { "termd" }, { "Causeway" },
                { "Causeway" } };

        for (int i = 0; i < queries.length; i++) {
            RemoteCollection<Project> projects = projectClient.getAll(Optional.of(sortQury), Optional.of(queries[i]));
            assertThat(projects).extracting(Project::getName).containsExactly(results[i]);
        }
    }

    @Test
    public void shouldFilterProjectsBasedOnNotLikeOperator() throws RemoteResourceException {
        String sortQury = "sort=asc=name";
        String[] queries = new String[] { "name=notlike=%De%", "name=notlike=%de%", "name=notlike=*de*",
                "name=notlike=P%", "name=notlike=P*", "name=notlike=%termd%", "name=notlike=_auseway",
                "name=notlike=?auseway" };
        String[][] results = new String[][] { // must be sorted lexicographically
                { "Causeway", "Pnc Build Agent", "termd" }, { "Causeway", "Pnc Build Agent", "termd" },
                { "Causeway", "Pnc Build Agent", "termd" }, { "Causeway", "Dependency Analysis", "termd" },
                { "Causeway", "Dependency Analysis", "termd" },
                { "Causeway", "Dependency Analysis", "Pnc Build Agent", "Project Newcastle Demo Project 1" },
                { "Dependency Analysis", "Pnc Build Agent", "Project Newcastle Demo Project 1", "termd" },
                { "Dependency Analysis", "Pnc Build Agent", "Project Newcastle Demo Project 1", "termd" } };

        for (int i = 0; i < queries.length; i++) {
            RemoteCollection<Project> projects = projectClient.getAll(Optional.of(sortQury), Optional.of(queries[i]));
            assertThat(projects).extracting(Project::getName).containsExactly(results[i]);
        }
    }

    @Test
    public void shouldFilterBuildConfigsWithAssociatedProductVersion() throws RemoteResourceException {
        String query = "productVersion=isnull=false";
        RemoteCollection<BuildConfiguration> bcs = buildConfigClient.getAll(empty(), Optional.of(query));
        assertThat(bcs).hasSize(3);
    }

    @Test
    public void shouldFilterBuildConfigsWithoutAssociatedProductVersion() throws RemoteResourceException {
        String query = "productVersion=isnull=true";
        RemoteCollection<BuildConfiguration> bcs = buildConfigClient.getAll(empty(), Optional.of(query));
        assertThat(bcs).hasSize(2);
    }

    @Test
    public void shouldFilterBuildConfigsByAssociatedGroupConfig() throws RemoteResourceException {
        String query = "groupConfigurations.name==Example-Build-Group-1";
        RemoteCollection<BuildConfiguration> bcs = buildConfigClient.getAll(empty(), Optional.of(query));
        assertThat(bcs).hasSize(3);
    }

    @Test
    public void shouldFilterUsingInOperator() throws RemoteResourceException {
        String query = "id=in=(1,2,1000,2000)";
        RemoteCollection<BuildConfiguration> bcs = buildConfigClient.getAll(empty(), Optional.of(query));
        assertThat(bcs).hasSize(2);
    }

    @Test
    public void shouldFilterUsingOutOperator() throws RemoteResourceException {
        String query = "id=out=(2,3,4,5,6,7,8,9,10,11)";
        RemoteCollection<BuildConfiguration> bcs = buildConfigClient.getAll(empty(), Optional.of(query));
        assertThat(bcs).hasSize(1);
    }

    @Test
    public void shouldLimitReturnedBuildConfigs() throws RemoteResourceException {
        BuildConfigPage page = given().accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .port(getHttpPort())
                .when()
                .get(BASE_REST_PATH + "/build-configs?pageSize=1")
                .then()
                .statusCode(200)
                .extract()
                .as(BuildConfigPage.class);
        assertThat(page.getPageSize()).isEqualTo(1);
        assertThat(page.getContent()).hasSize(1);
    }

}
