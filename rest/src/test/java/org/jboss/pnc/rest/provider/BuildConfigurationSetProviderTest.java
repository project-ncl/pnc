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

import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.validation.exceptions.InvalidEntityException;
import org.jboss.pnc.rest.validation.exceptions.RestValidationException;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationSetRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import java.util.LinkedList;
import java.util.List;

import static java.util.stream.Collectors.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;


public class BuildConfigurationSetProviderTest {

    @Mock
    private BuildConfigurationSetRepository buildConfigurationSetRepository;

    @Mock
    private BuildConfigurationRepository buildConfigurationRepository;

    @InjectMocks
    private BuildConfigurationSetProvider buildConfigurationSetProvider;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        when(buildConfigurationSetRepository.queryById(1)).thenReturn(createBuildConfigurationSet(1));
        when(buildConfigurationSetRepository.queryById(2)).thenReturn(null);

        when(buildConfigurationRepository.queryById(1)).thenReturn(createBuildConfiguration(1));
        when(buildConfigurationRepository.queryById(2)).thenReturn(createBuildConfiguration(2));
        when(buildConfigurationRepository.queryById(3)).thenReturn(null);
    }

    @Test
    public void shouldThrowConflictedEntryExceptionWhenAddingDuplicatedConfiguration() {
        //given
        BuildRecordProvider buildRecordProvider = mock(BuildRecordProvider.class);
        BuildConfigurationSetRepository buildConfigurationSetRepository = mock(BuildConfigurationSetRepository.class);
        BuildConfigurationRepository buildConfigurationRepository = mock(BuildConfigurationRepository.class);
        BuildConfigurationProvider buildConfigurationProvider = mock(BuildConfigurationProvider.class);

        BuildConfigurationSet testBCS = createBuildConfigurationSet(1);
        when(buildConfigurationSetRepository.queryById(1)).thenReturn(testBCS);
        when(buildConfigurationRepository.queryById(2)).thenReturn(createBuildConfiguration(2));

        BuildConfigurationSetProvider buildConfigurationSetProvider = new BuildConfigurationSetProvider(buildConfigurationSetRepository, buildConfigurationRepository, null, null, null);

        //when
        addConfigsToSet(testBCS, createBuildConfiguration(1), createBuildConfiguration(2));
        try {
            buildConfigurationSetProvider.addConfiguration(1, 2);
            fail();
        } catch (RestValidationException ignoreMe) {
            //then
        }
    }

    @Test
    public void shouldAddNewBuildConfigurationToSet() throws Exception {
        BuildRecordProvider buildRecordProvider = mock(BuildRecordProvider.class);
        BuildConfigurationSetRepository buildConfigurationSetRepository = mock(BuildConfigurationSetRepository.class);
        BuildConfigurationRepository buildConfigurationRepository = mock(BuildConfigurationRepository.class);
        BuildConfigurationProvider buildConfigurationProvider = mock(BuildConfigurationProvider.class);

        BuildConfigurationSet testBCS = createBuildConfigurationSet(1);

        when(buildConfigurationSetRepository.queryById(1)).thenReturn(testBCS);
        when(buildConfigurationRepository.queryById(3)).thenReturn(createBuildConfiguration(3));

        BuildConfigurationSetProvider buildConfigurationSetProvider = new BuildConfigurationSetProvider(buildConfigurationSetRepository, buildConfigurationRepository, null, null, null);

        //when
        addConfigsToSet(testBCS, createBuildConfiguration(1), createBuildConfiguration(2));
        buildConfigurationSetProvider.addConfiguration(1,3);

        //then
        assertThat(testBCS.getBuildConfigurations()).hasSize(3);
    }

    @Test
    public void shouldUpdateBuildConfigurations() throws Exception {
        //given
        List<BuildConfigurationRest> buildConfigurationRests = new LinkedList<>();
        buildConfigurationRests.add(new BuildConfigurationRest(createBuildConfiguration(2)));

        ArgumentCaptor<BuildConfigurationSet> args = ArgumentCaptor.forClass(BuildConfigurationSet.class);

        //when
        buildConfigurationSetProvider.updateConfigurations(1, buildConfigurationRests);

        //then
        verify(buildConfigurationSetRepository, times(1)).save(args.capture());
        assertThat(args.getValue().getBuildConfigurations()).hasSize(1);
        assertThat(args.getValue().getBuildConfigurations().stream().map(BuildConfiguration::getId).collect(toList())).containsOnly(2);
    }

    @Test
    public void shouldUpdateBuildConfigurationsWithEmptyList() throws Exception {
        //given
        List<BuildConfigurationRest> buildConfigurationRests = new LinkedList<>();

        ArgumentCaptor<BuildConfigurationSet> args = ArgumentCaptor.forClass(BuildConfigurationSet.class);

        //when
        buildConfigurationSetProvider.updateConfigurations(1, buildConfigurationRests);

        //then
        verify(buildConfigurationSetRepository, times(1)).save(args.capture());
        assertThat(args.getValue().getBuildConfigurations()).isEmpty();
    }

    @Test(expected = InvalidEntityException.class)
    public void shouldThrowInvalidEntityExceptionWhenUpdatingAllBuildConfigurationsAndBuildConfigurationSetDoesNotExist() throws Exception {
        //given
        List<BuildConfigurationRest> buildConfigurationRests = new LinkedList<>();

        //when
        buildConfigurationSetProvider.updateConfigurations(2, buildConfigurationRests);

        //then expect InvalidEntityException to be thrown
    }

    @Test(expected = InvalidEntityException.class)
    public void shouldThrowInvalidEntityExceptionWhenUpdatingAllBuildConfigurationsAndABuildConfigurationDoesNotExist() throws Exception {
        //given
        List<BuildConfigurationRest> buildConfigurationRests = new LinkedList<>();
        buildConfigurationRests.add(new BuildConfigurationRest(createBuildConfiguration(3)));

        //when
        buildConfigurationSetProvider.updateConfigurations(1, buildConfigurationRests);

        //then expect InvalidEntityException to be thrown
    }

    private BuildConfiguration createBuildConfiguration(int id){
        return BuildConfiguration.Builder.newBuilder().id(id).build();
    }

    private BuildConfigurationSet createBuildConfigurationSet(int id){
        BuildConfigurationSet retVal = new BuildConfigurationSet();
        retVal.setId(id);
        return retVal;
    }

    private void addConfigsToSet(BuildConfigurationSet bset, BuildConfiguration... bconfigs){
        for (BuildConfiguration config : bconfigs){
            bset.addBuildConfiguration(config);
        }
    }

}
