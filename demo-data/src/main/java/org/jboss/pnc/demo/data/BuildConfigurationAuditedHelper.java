/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.demo.data;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;

/**
 * The purpose of this class is to have some methods in separate new transactions.
 *
 * @author Adam Kridl &lt;akridl@redhat.com&gt;
 */
@ApplicationScoped
public class BuildConfigurationAuditedHelper {

    @Inject
    private BuildConfigurationRepository repository;

    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    Integer save(BuildConfiguration buildConfiguration) {
        BuildConfiguration buildConfig = repository.save(buildConfiguration);
        return buildConfig.getId();
    }
}
