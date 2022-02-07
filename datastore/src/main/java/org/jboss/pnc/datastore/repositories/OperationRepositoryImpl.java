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

import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.datastore.repositories.internal.OperationSpringRepository;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.Operation;
import org.jboss.pnc.spi.datastore.repositories.OperationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class OperationRepositoryImpl extends AbstractRepository<Operation, Base32LongID>
        implements OperationRepository {

    private static final Logger logger = LoggerFactory.getLogger(OperationRepositoryImpl.class);

    private OperationSpringRepository repository;

    /**
     * @deprecated Created for CDI.
     */
    @Deprecated
    public OperationRepositoryImpl() {
        super(null, null);
    }

    @Inject
    public OperationRepositoryImpl(OperationSpringRepository repository) {
        super(repository, repository);
        this.repository = repository;
    }
}
