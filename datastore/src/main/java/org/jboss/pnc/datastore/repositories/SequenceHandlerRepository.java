package org.jboss.pnc.datastore.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.dialect.Dialect;
import org.hibernate.jdbc.ReturningWork;
import org.hibernate.jdbc.Work;
import org.hibernate.service.jdbc.dialect.internal.StandardDialectResolver;
import org.hibernate.service.jdbc.dialect.spi.DialectResolver;
import org.jboss.pnc.model.BuildRecord;

//@ApplicationScoped
public class SequenceHandlerRepository {

    public SequenceHandlerRepository() {

    }

    private EntityManager entityManager;

    @Inject
    public SequenceHandlerRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
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

    public void insertBuildRecordBypassingSequence(final BuildRecord br) {

        Work insertWork = new Work() {
            @Override
            public void execute(Connection connection) throws SQLException {
                PreparedStatement preparedStatement = null;
                ResultSet resultSet = null;
                try {
                    preparedStatement = connection.prepareStatement(BuildRecord.PREPARED_STATEMENT_INSERT);
                    preparedStatement.setInt(1, br.getId());
                    preparedStatement.setString(2, br.getBuildContentId());
                    preparedStatement.setString(3, br.getBuildDriverId());
                    preparedStatement.setString(4, br.getBuildLog());
                    preparedStatement.setTimestamp(5, br.getEndTime());
                    preparedStatement.setTimestamp(6, br.getStartTime());

                    if (br.getStatus() != null) {
                        preparedStatement.setString(7, br.getStatus().toString());
                    } else {
                        preparedStatement.setNull(7, Types.VARCHAR);
                    }

                    if (br.getBuildConfigurationAudited() != null) {
                        preparedStatement.setInt(8, br.getBuildConfigurationAudited().getId());
                        preparedStatement.setInt(9, br.getBuildConfigurationAudited().getRev());
                    } else {
                        preparedStatement.setNull(8, Types.NULL);
                        preparedStatement.setNull(9, Types.NULL);
                    }

                    if (br.getUser() != null) {
                        preparedStatement.setInt(10, br.getUser().getId());
                    } else {
                        preparedStatement.setNull(10, Types.INTEGER);
                    }

                    if (br.getSystemImage() != null) {
                        preparedStatement.setInt(11, br.getSystemImage().getId());
                    } else {
                        preparedStatement.setNull(11, Types.INTEGER);
                    }

                    if (br.getBuildConfigSetRecord() != null) {
                        preparedStatement.setInt(12, br.getBuildConfigSetRecord().getId());
                    } else {
                        preparedStatement.setNull(12, Types.INTEGER);
                    }

                    preparedStatement.executeUpdate();
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

        sessionFactory.getCurrentSession().doWork(insertWork);
    }

}
