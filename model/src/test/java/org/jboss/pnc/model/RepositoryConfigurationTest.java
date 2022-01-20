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
package org.jboss.pnc.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
