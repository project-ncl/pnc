package org.jboss.pnc.rest.provider;

import org.jboss.pnc.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.datastore.repositories.BuildConfigurationSetRepository;
import org.jboss.pnc.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.junit.Test;
import org.mockito.Mockito;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Created by aabulawi on 05/06/15.
 */
public class BuildConfigurationSetProviderTest {

    @Test
    public void addExistingBuildConfigurationToSet(){
        BuildConfigurationSetRepository buildConfigurationSetRepository = mock(BuildConfigurationSetRepository.class);
        BuildConfigurationRepository buildConfigurationRepository = mock(BuildConfigurationRepository.class);
        BuildRecordRepository buildRecordRepository = mock(BuildRecordRepository.class);

        BuildConfigurationSet testBCS = createBuildConfigurationSet(1);
        addConfigsToSet(testBCS, createBuildConfiguration(1), createBuildConfiguration(2));

        Mockito.when(buildConfigurationSetRepository.findOne(1)).thenReturn(testBCS);
        Mockito.when(buildConfigurationRepository.findOne(2)).thenReturn(createBuildConfiguration(2));

        BuildConfigurationSetProvider buildConfigurationSetProvider = new BuildConfigurationSetProvider(buildConfigurationSetRepository, buildRecordRepository, buildConfigurationRepository);
        assertEquals(409, buildConfigurationSetProvider.addConfiguration(1,2).getStatus());
    }

    @Test
    public void addNewBuildConfigurationToSet(){
        BuildConfigurationSetRepository buildConfigurationSetRepository = mock(BuildConfigurationSetRepository.class);
        BuildConfigurationRepository buildConfigurationRepository = mock(BuildConfigurationRepository.class);
        BuildRecordRepository buildRecordRepository = mock(BuildRecordRepository.class);

        BuildConfigurationSet testBCS = createBuildConfigurationSet(1);
        addConfigsToSet(testBCS, createBuildConfiguration(1), createBuildConfiguration(2));

        Mockito.when(buildConfigurationSetRepository.findOne(1)).thenReturn(testBCS);
        Mockito.when(buildConfigurationRepository.findOne(3)).thenReturn(createBuildConfiguration(3));

        BuildConfigurationSetProvider buildConfigurationSetProvider = new BuildConfigurationSetProvider(buildConfigurationSetRepository, buildRecordRepository, buildConfigurationRepository);
        assertEquals(200, buildConfigurationSetProvider.addConfiguration(1,3).getStatus());
    }

    private BuildConfiguration createBuildConfiguration(int id){
        BuildConfiguration retVal = new BuildConfiguration();
        retVal.setId(id);
        return retVal;
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
