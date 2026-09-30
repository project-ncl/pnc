/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mock.repository;

import java.util.HashMap;
import java.util.Map;

import org.jboss.pnc.spi.datastore.repositories.SequenceHandlerRepository;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 9/22/16 Time: 2:20 PM
 */
public class SequenceHandlerRepositoryMock implements SequenceHandlerRepository {
    private final Map<String, Long> sequences = new HashMap<>();

    @Override
    public String getEntityManagerFactoryProperty(String propertyName) {
        return null;
    }

    @Override
    public synchronized Long getNextID(String sequenceName) {
        init(sequenceName);
        Long next = sequences.get(sequenceName);
        sequences.put(sequenceName, ++next);
        return next;
    }

    @Override
    public void createSequence(String sequenceName) {
    }

    @Override
    public boolean sequenceExists(String sequenceName) {
        return true;
    }

    @Override
    public void dropSequence(String sequenceName) {
    }

    private void init(String sequenceName) {
        if (!sequences.containsKey(sequenceName)) {
            sequences.put(sequenceName, 0L);
        }
    }
}
