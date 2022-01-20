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
package org.jboss.pnc.mock.repository;

import org.jboss.pnc.spi.datastore.repositories.SequenceHandlerRepository;

import java.util.HashMap;
import java.util.Map;

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
