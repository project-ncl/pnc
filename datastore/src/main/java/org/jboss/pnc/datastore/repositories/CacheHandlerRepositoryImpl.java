/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
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
