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

package org.jboss.pnc.rest.provider;

import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.rest.provider.collection.CollectionInfo;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.validation.exceptions.RestValidationException;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class AbstractProviderTest {

    class TestedAbstractProvider extends AbstractProvider<BuildConfiguration, BuildConfigurationRest> {

        public TestedAbstractProvider() {
            super(
                    AbstractProviderTest.this.repository,
                    AbstractProviderTest.this.rsqlPredicateProducer,
                    AbstractProviderTest.this.sortInfoProducer,
                    AbstractProviderTest.this.pageInfoProducer);
        }

        @Override
        protected Function<? super BuildConfiguration, ? extends BuildConfigurationRest> toRESTModel() {
            return buildConfiguration -> new BuildConfigurationRest(buildConfiguration);
        }

        @Override
        protected Function<? super BuildConfigurationRest, ? extends BuildConfiguration> toDBModel() {
            return buildConfigurationRest -> BuildConfiguration.Builder.newBuilder()
                    .id(buildConfigurationRest.getId())
                    .build();
        }

    }

    private RSQLPredicateProducer rsqlPredicateProducer;
    private SortInfoProducer sortInfoProducer;
    private PageInfoProducer pageInfoProducer;
    private Repository<BuildConfiguration, Integer> repository;

    @Before
    public void before() {
        rsqlPredicateProducer = mock(RSQLPredicateProducer.class);
        sortInfoProducer = mock(SortInfoProducer.class);
        pageInfoProducer = mock(PageInfoProducer.class);
        repository = mock(Repository.class);
    }

    @Test
    public void shouldReturnCollectionWithPagingInfo() throws Exception {
        // given
        BuildConfiguration exampleConfiguration = BuildConfiguration.Builder.newBuilder().build();

        TestedAbstractProvider testedAbstractProvider = new TestedAbstractProvider();
        doReturn(Arrays.asList(exampleConfiguration)).when(repository).queryWithPredicates(any(), any(), any());
        doReturn(100).when(repository).count(any());

        // when
        CollectionInfo<BuildConfigurationRest> returnedCollection = testedAbstractProvider
                .getAll(0, 10, "sort", "query");

        // when
        assertThat(returnedCollection.getPageIndex()).isEqualTo(0);
        assertThat(returnedCollection.getPageSize()).isEqualTo(10);
        assertThat(returnedCollection.getTotalPages()).isEqualTo(10);
        assertThat(returnedCollection.getContent().size()).isEqualTo(1);
    }

    @Test
    public void shouldReturnSingleton() throws Exception {
        // given
        BuildConfiguration exampleConfiguration = BuildConfiguration.Builder.newBuilder().id(1).build();

        TestedAbstractProvider testedAbstractProvider = new TestedAbstractProvider();
        doReturn(exampleConfiguration).when(repository).queryById(1);

        // when
        BuildConfigurationRest returnedSingleton = testedAbstractProvider.getSpecific(1);

        // when
        assertThat(returnedSingleton.getId()).isEqualTo(1);
    }

    @Test
    public void shouldCallStore() throws Exception {
        // given
        BuildConfiguration exampleConfiguration = BuildConfiguration.Builder.newBuilder().id(1).build();

        BuildConfigurationRest exampleConfigurationRest = new BuildConfigurationRest(exampleConfiguration);

        TestedAbstractProvider testedAbstractProvider = new TestedAbstractProvider() {
            @Override
            protected void validateBeforeSaving(BuildConfigurationRest restEntity) throws RestValidationException {

            }
        };
        doReturn(exampleConfiguration).when(repository).save(any());

        // when
        Integer id = testedAbstractProvider.store(exampleConfigurationRest);

        // when
        assertThat(id).isEqualTo(1);
    }

}