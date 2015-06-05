package org.jboss.pnc.datastore.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.dialect.Dialect;
import org.hibernate.jdbc.ReturningWork;
import org.hibernate.jdbc.Work;
import org.hibernate.service.jdbc.dialect.internal.StandardDialectResolver;
import org.hibernate.service.jdbc.dialect.spi.DialectResolver;

public class SequenceHandlerRepository {

    public SequenceHandlerRepository() {

    }

    private EntityManager entityManager;
    private Map<String, Object> entityManagerFactoryProperties;

    @Inject
    public SequenceHandlerRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.entityManagerFactoryProperties = entityManager.getEntityManagerFactory().getProperties();
    }

    public String getEntityManagerFactoryProperty(String propertyName) {
        for (Map.Entry<String, Object> e : entityManagerFactoryProperties.entrySet()) {
            if (e.getKey().trim().equals(propertyName)) {
                return e.getValue().toString();
            }
        }
        return null;
    }

    public Long getNextID(final String sequenceName) {

        ReturningWork<Long> maxReturningWork = new ReturningWork<Long>() {
            @Override
            public Long execute(Connection connection) throws SQLException {
                DialectResolver dialectResolver = new StandardDialectResolver();
                Dialect dialect = dialectResolver.resolveDialect(connection.getMetaData());
                PreparedStatement preparedStatement = null;
                ResultSet resultSet = null;
                try {
                    preparedStatement = connection.prepareStatement(dialect.getSequenceNextValString(sequenceName));
                    resultSet = preparedStatement.executeQuery();
                    resultSet.next();
                    return resultSet.getLong(1);
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

            }
        };

        Session session = (Session) entityManager.getDelegate();
        SessionFactory sessionFactory = session.getSessionFactory();

        Long maxRecord = sessionFactory.getCurrentSession().doReturningWork(maxReturningWork);
        return maxRecord;
    }

    public void createSequence(final String sequenceName) {

        Work work = new Work() {
            @Override
            public void execute(Connection connection) throws SQLException {
                DialectResolver dialectResolver = new StandardDialectResolver();
                Dialect dialect = dialectResolver.resolveDialect(connection.getMetaData());
                PreparedStatement preparedStatement = null;
                ResultSet resultSet = null;
                try {
                    preparedStatement = connection.prepareStatement(dialect.getCreateSequenceStrings(sequenceName, 1, 1)[0]);
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

            }
        };

        Session session = (Session) entityManager.getDelegate();
        SessionFactory sessionFactory = session.getSessionFactory();
        sessionFactory.getCurrentSession().doWork(work);
    }

    public void dropSequence(final String sequenceName) {

        Work work = new Work() {
            @Override
            public void execute(Connection connection) throws SQLException {
                DialectResolver dialectResolver = new StandardDialectResolver();
                Dialect dialect = dialectResolver.resolveDialect(connection.getMetaData());
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

            }
        };

        Session session = (Session) entityManager.getDelegate();
        SessionFactory sessionFactory = session.getSessionFactory();
        sessionFactory.getCurrentSession().doWork(work);
    }

}
