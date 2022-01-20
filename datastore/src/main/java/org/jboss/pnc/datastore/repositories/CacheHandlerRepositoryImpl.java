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
package org.jboss.pnc.datastore.repositories;

import java.util.Map;
import java.util.SortedMap;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.jboss.pnc.model.utils.HibernateMetric;
import org.jboss.pnc.model.utils.HibernateStatsUtils;
import org.jboss.pnc.spi.datastore.repositories.CacheHandlerRepository;

@Stateless
public class CacheHandlerRepositoryImpl implements CacheHandlerRepository {

    public CacheHandlerRepositoryImpl() {
    }

    private EntityManager entityManager;

    @Inject
    public CacheHandlerRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public SortedMap<String, Map<String, HibernateMetric>> getSecondLevelCacheEntitiesStats() {
        SessionFactory sessionFactory = ((Session) entityManager.getDelegate()).getSessionFactory();
        Statistics statistics = sessionFactory.getStatistics();
        return HibernateStatsUtils.getSecondLevelCacheEntitiesStats(statistics);
    }

    @Override
    public SortedMap<String, Map<String, HibernateMetric>> getSecondLevelCacheRegionsStats() {
        SessionFactory sessionFactory = ((Session) entityManager.getDelegate()).getSessionFactory();
        Statistics statistics = sessionFactory.getStatistics();
        return HibernateStatsUtils.getSecondLevelCacheRegionsStats(statistics);
    }

    @Override
    public SortedMap<String, Map<String, HibernateMetric>> getSecondLevelCacheCollectionsStats() {
        SessionFactory sessionFactory = ((Session) entityManager.getDelegate()).getSessionFactory();
        Statistics statistics = sessionFactory.getStatistics();
        return HibernateStatsUtils.getSecondLevelCacheCollectionsStats(statistics);
    }

    @Override
    public SortedMap<String, HibernateMetric> getGenericStats() {
        SessionFactory sessionFactory = ((Session) entityManager.getDelegate()).getSessionFactory();
        Statistics statistics = sessionFactory.getStatistics();
        return HibernateStatsUtils.getGenericStats(statistics);
    }

    @Override
    public void clearCache() {
        entityManager.getEntityManagerFactory().getCache().evictAll();
    }

}
