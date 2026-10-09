/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.datastore.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.dialect.internal.StandardDialectResolver;
import org.hibernate.engine.jdbc.dialect.spi.DatabaseMetaDataDialectResolutionInfoAdapter;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolver;
import org.hibernate.jdbc.ReturningWork;
import org.hibernate.jdbc.Work;
import org.jboss.pnc.spi.datastore.repositories.SequenceHandlerRepository;

@Dependent
public class DefaultSequenceHandlerRepository implements SequenceHandlerRepository {

    public DefaultSequenceHandlerRepository() {

    }

    private EntityManager entityManager;
    private Map<String, Object> entityManagerFactoryProperties;

    @Inject
    public DefaultSequenceHandlerRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.entityManagerFactoryProperties = entityManager.getEntityManagerFactory().getProperties();
    }

    @Override
    public String getEntityManagerFactoryProperty(String propertyName) {
        for (Map.Entry<String, Object> e : entityManagerFactoryProperties.entrySet()) {
            if (e.getKey().trim().equals(propertyName)) {
                return e.getValue().toString();
            }
        }
        return null;
    }

    @Override
    public Long getNextID(final String sequenceName) {

        ReturningWork<Long> maxReturningWork = connection -> {
            DialectResolver dialectResolver = new StandardDialectResolver();
            Dialect dialect = dialectResolver.resolveDialect(getResolutionInfo(connection));
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement(dialect.getSequenceNextValString(sequenceName));
                    ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            } catch (SQLException e) {
                throw e;
            }

        };

        Session session = (Session) entityManager.getDelegate();
        SessionFactory sessionFactory = session.getSessionFactory();

        Long maxRecord = sessionFactory.getCurrentSession().doReturningWork(maxReturningWork);
        return maxRecord;
    }

    @Override
    public void createSequence(final String sequenceName) {

        if (sequenceExists(sequenceName)) {
            return;
        }
        Work work = connection -> {
            DialectResolver dialectResolver = new StandardDialectResolver();
            Dialect dialect = dialectResolver.resolveDialect(getResolutionInfo(connection));
            PreparedStatement preparedStatement = null;
            ResultSet resultSet = null;
            try {
                preparedStatement = connection
                        .prepareStatement(dialect.getCreateSequenceStrings(sequenceName, 1, 1)[0]);
                preparedStatement.execute();
            } catch (SQLException e) {
                throw e;
            } finally {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
            }

        };

        Session session = (Session) entityManager.getDelegate();
        SessionFactory sessionFactory = session.getSessionFactory();
        sessionFactory.getCurrentSession().doWork(work);
    }

    public DatabaseMetaDataDialectResolutionInfoAdapter getResolutionInfo(Connection connection) throws SQLException {
        return new DatabaseMetaDataDialectResolutionInfoAdapter(connection.getMetaData());
    }

    @Override
    public boolean sequenceExists(final String sequenceName) {
        ReturningWork<Boolean> work = connection -> {
            DialectResolver dialectResolver = new StandardDialectResolver();
            Dialect dialect = dialectResolver.resolveDialect(getResolutionInfo(connection));
            try (PreparedStatement preparedStatement = connection.prepareStatement(dialect.getQuerySequencesString());
                    ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    if (sequenceName.equals(resultSet.getString(1))) {
                        return true;
                    }
                }
            } catch (SQLException e) {
                throw e;
            }
            return false;

        };

        Session session = (Session) entityManager.getDelegate();
        SessionFactory sessionFactory = session.getSessionFactory();
        return sessionFactory.getCurrentSession().doReturningWork(work);
    }

    @Override
    public void dropSequence(final String sequenceName) {

        Work work = connection -> {
            DialectResolver dialectResolver = new StandardDialectResolver();
            Dialect dialect = dialectResolver.resolveDialect(getResolutionInfo(connection));
            PreparedStatement preparedStatement = null;
            ResultSet resultSet = null;
            try {
                preparedStatement = connection.prepareStatement(dialect.getDropSequenceStrings(sequenceName)[0]);
                preparedStatement.execute();
            } catch (SQLException e) {
                throw e;
            } finally {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
            }

        };

        Session session = (Session) entityManager.getDelegate();
        SessionFactory sessionFactory = session.getSessionFactory();
        sessionFactory.getCurrentSession().doWork(work);
    }

}
