/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.datastore.repositories;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.jboss.pnc.spi.datastore.repositories.HibernateSearchHandlerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class HibernateSearchHandlerRepositoryImpl implements HibernateSearchHandlerRepository {

    Logger logger = LoggerFactory.getLogger(HibernateSearchHandlerRepositoryImpl.class);

    EntityManager entityManager;

    @Deprecated // CDI workaround
    public HibernateSearchHandlerRepositoryImpl() {
    }

    @Inject
    public HibernateSearchHandlerRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void startFullIndexing() {

        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        try {
            fullTextEntityManager.createIndexer().startAndWait();
        } catch (InterruptedException e) {
            logger.error("Error while doing full indexing", e);
        }

    }

}
