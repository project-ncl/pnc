/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.datastore.repositories;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 9/22/16 Time: 2:21 PM
 */
public interface SequenceHandlerRepository {
    String getEntityManagerFactoryProperty(String propertyName);

    Long getNextID(String sequenceName);

    void createSequence(String sequenceName);

    boolean sequenceExists(String sequenceName);

    void dropSequence(String sequenceName);
}
