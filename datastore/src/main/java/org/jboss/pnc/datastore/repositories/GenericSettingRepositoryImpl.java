/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.datastore.repositories;

import javax.ejb.Stateless;

import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.model.GenericSetting;
import org.jboss.pnc.spi.datastore.predicates.GenericSettingPredicates;
import org.jboss.pnc.spi.datastore.repositories.GenericSettingRepository;

@Stateless
public class GenericSettingRepositoryImpl extends AbstractRepository<GenericSetting, Integer>
        implements GenericSettingRepository {

    public GenericSettingRepositoryImpl() {
        super(GenericSetting.class, Integer.class);
    }

    @Override
    public GenericSetting queryByKey(String key) {
        return queryByPredicates(GenericSettingPredicates.withKey(key));
    }
}
