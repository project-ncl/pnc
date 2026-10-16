/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.persistence.EntityManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class RepositoryConfigurationTest extends AbstractModelTest {

    String internalScmHost = "internal.host";
    String internalScmPath = "/my/repo";
    String internalScmPort = ":123";

    private EntityManager em;

    @Before
    public void init() throws Exception {
        em = getEmFactory().createEntityManager();
    }

    @After
    public void cleanup() {
        clearDatabaseTables();
        em.close();
    }

    @Test
    public void shouldStoreNormalizedScms() {
        // given
        String internalScmBase = internalScmHost + internalScmPort + internalScmPath;
        String internalScmWithoutPort = internalScmHost + internalScmPath;
        String externalScmBase = "github.com/my/repo";

        RepositoryConfiguration repositoryConfiguration = RepositoryConfiguration.Builder.newBuilder()
                .internalUrl("git-ssh://git@" + internalScmBase + ".git")
                .externalUrl("https://git@" + externalScmBase + ".git")
                .build();

        // when
        em.getTransaction().begin();
        em.persist(repositoryConfiguration);
        em.getTransaction().commit();

        // then
        RepositoryConfiguration obtained = em.find(RepositoryConfiguration.class, repositoryConfiguration.getId());
        assertNotNull(obtained.getInternalUrlNormalized());
        assertEquals(internalScmWithoutPort, obtained.getInternalUrlNormalized());

        assertNotNull(obtained.getExternalUrlNormalized());
        assertEquals(externalScmBase, obtained.getExternalUrlNormalized());
    }

}
