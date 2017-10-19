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
package org.jboss.pnc.rest.provider;

import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.rest.restmodel.BuildConfigurationSetRest;
import org.jboss.pnc.rest.validation.exceptions.ConflictedEntryException;
import org.jboss.pnc.rest.validation.exceptions.InvalidEntityException;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationSetRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductVersionRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * @author Alex Creasy
 */
public class ProductVersionProviderTest {

    private ProductVersionProvider productVersionProvider;
    private ProductVersionRepository mockProductVersionRepository;
    private BuildConfigurationSetRepository mockBuildConfigurationSetRepository;

    private Product product1;

    private ProductVersion productVersion1;
    private ProductVersion productVersion2;

    private BuildConfigurationSet buildConfigurationSet1;
    private BuildConfigurationSet buildConfigurationSet2;
    private BuildConfigurationSet buildConfigurationSet3;


    @Before
    public void setup() {
        mockProductVersionRepository = mock(ProductVersionRepository.class);
        mockBuildConfigurationSetRepository = mock(BuildConfigurationSetRepository.class);
        productVersionProvider = new ProductVersionProvider(mockProductVersionRepository, mockBuildConfigurationSetRepository, null, null, null, null);

        product1 = Product.Builder.newBuilder().id(1).name("product-1").build();

        productVersion1 = ProductVersion.Builder.newBuilder().id(1).version("1.0").product(product1)
                .generateBrewTagPrefix("TMP", "1.0").build();
        productVersion2 = ProductVersion.Builder.newBuilder().id(2).version("2.0").product(product1)
                .generateBrewTagPrefix("TMP", "2.0").build();

        when(mockProductVersionRepository.queryById(1)).thenReturn(productVersion1);
        when(mockProductVersionRepository.queryById(2)).thenReturn(productVersion2);


        buildConfigurationSet1 = BuildConfigurationSet.Builder.newBuilder().id(1).name("bcs-1").productVersion(productVersion1).build();
        buildConfigurationSet2 = BuildConfigurationSet.Builder.newBuilder().id(2).name("bcs-2").productVersion(productVersion2).build();
        buildConfigurationSet3 = BuildConfigurationSet.Builder.newBuilder().id(3).name("bcs-3").build();

        when(mockBuildConfigurationSetRepository.queryById(1)).thenReturn(buildConfigurationSet1);
        when(mockBuildConfigurationSetRepository.queryById(2)).thenReturn(buildConfigurationSet2);
        when(mockBuildConfigurationSetRepository.queryById(3)).thenReturn(buildConfigurationSet3);
    }


    @Test
    public void shouldUpdateBuildConfigurationSets() throws Exception {
        // Given
        List<BuildConfigurationSetRest> buildConfigurationSets = new LinkedList<>();
        buildConfigurationSets.add(new BuildConfigurationSetRest(buildConfigurationSet3));

        ArgumentCaptor<BuildConfigurationSet> args = ArgumentCaptor.forClass(BuildConfigurationSet.class);

        // When
        productVersionProvider.updateBuildConfigurationSets(1, buildConfigurationSets);

        // Then
        verify(mockBuildConfigurationSetRepository, times(1)).save(args.capture());
        assertThat(args.getValue().getId()).isEqualTo(3);
        assertThat(args.getValue().getProductVersion().getId()).isEqualTo(1);
    }

    @Test(expected = InvalidEntityException.class)
    public void shouldThrowInvalidEntityExceptionWhenNullSecondArg() throws Exception {
        // When
        productVersionProvider.updateBuildConfigurationSets(1, null);

        // Then expect InvalidEntityException
    }

    @Test(expected = InvalidEntityException.class)
    public void shouldThrowInvalidEntityExceptionWithInvalidBuildConfigurationSets() throws Exception {
        // Given
        BuildConfigurationSetRest restModel = new BuildConfigurationSetRest(BuildConfigurationSet.Builder.newBuilder().id(9999).name("i-dont-exist").build());
        when(mockBuildConfigurationSetRepository.queryById(9999)).thenReturn(null);

        List<BuildConfigurationSetRest> buildConfigurationSetRests = new LinkedList<>();
        buildConfigurationSetRests.add(restModel);

        // When
        productVersionProvider.updateBuildConfigurationSets(1, buildConfigurationSetRests);

        // Then expect InvalidEntityException.
    }

    @Test(expected = ConflictedEntryException.class)
    public void shouldThrowConflictedEntryExceptionWhenAddingBCSetsThatAreAlreadyAssociatedWithAProductVersion() throws Exception {
        // Given
        List<BuildConfigurationSetRest> buildConfigurationSets = new LinkedList<>();
        buildConfigurationSets.add(new BuildConfigurationSetRest(buildConfigurationSet2));

        // When
        productVersionProvider.updateBuildConfigurationSets(1, buildConfigurationSets);

        // Then expect ConflictedEntryException.
    }

}
