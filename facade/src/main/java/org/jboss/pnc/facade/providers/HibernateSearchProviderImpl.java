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
package org.jboss.pnc.facade.providers;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.pnc.facade.providers.api.HibernateSearchProvider;
import org.jboss.pnc.spi.datastore.repositories.HibernateSearchHandlerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PermitAll
@Stateless
public class HibernateSearchProviderImpl implements HibernateSearchProvider {

    private static final Logger logger = LoggerFactory.getLogger(HibernateSearchProviderImpl.class);

    private HibernateSearchHandlerRepository hibernateSearchHandlerRepository;

    @Inject
    public HibernateSearchProviderImpl(HibernateSearchHandlerRepository hibernateSearchHandlerRepository) {
        this.hibernateSearchHandlerRepository = hibernateSearchHandlerRepository;
    }

    @Override
    public void startFullIndexing() {
        logger.info("Start Hibernate Search full indexing...");
        hibernateSearchHandlerRepository.startFullIndexing();
    }

}
