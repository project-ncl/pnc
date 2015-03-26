package org.jboss.pnc.mavenrepositorymanager;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.commonjava.aprox.client.core.Aprox;
import org.commonjava.aprox.model.core.Group;
import org.commonjava.aprox.model.core.StoreKey;
import org.commonjava.aprox.model.core.StoreType;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.spi.repositorymanager.model.RepositoryConfiguration;
import org.junit.Test;

public class RepositoryManagerDriver05Test extends AbstractRepositoryManagerDriverTest {

    @Test
    public void verifyGroupComposition_ProjectVersion_NoConfSet() throws Exception {
        final BuildConfiguration pbc = simpleBuildConfiguration();

        final BuildRecordSet bc = new BuildRecordSet();
        bc.setProductVersion( pbc.getProductVersion() );

        final Aprox aprox = driver.getAprox();

        final RepositoryConfiguration repositoryConfiguration = driver.createRepository( pbc, bc );
        final String repoId = repositoryConfiguration.getId();

        final Group buildGroup = aprox.stores().load(StoreType.group, repoId, Group.class);
        assertGroupConstituents(buildGroup, new StoreKey(StoreType.hosted, repoId),
                                 new StoreKey( StoreType.group, "product+myproduct+1-0" ),
                                 new StoreKey( StoreType.hosted,
                        RepositoryManagerDriver.SHARED_RELEASES_ID), new StoreKey(StoreType.hosted,
                        RepositoryManagerDriver.SHARED_IMPORTS_ID), new StoreKey(StoreType.group,
                        RepositoryManagerDriver.PUBLIC_GROUP_ID));
    }

    private void assertGroupConstituents(final Group buildGroup, final StoreKey... constituents) {
        final List<StoreKey> groupConstituents = buildGroup.getConstituents();
        for (int i = 0; i < constituents.length; i++) {
            assertThat("Group constituency too small to contain all the expected members.", groupConstituents.size() > i,
                    equalTo(true));

            final StoreKey expected = constituents[i];
            final StoreKey actual = groupConstituents.get(i);
            assertThat(actual, equalTo(expected));
        }
    }
}
