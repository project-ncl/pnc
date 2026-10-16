/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.datastore.predicates;

import org.jboss.pnc.model.GenericSetting;
import org.jboss.pnc.model.GenericSetting_;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;

/**
 * Predicates for {@link org.jboss.pnc.model.GenericSetting} entity.
 */
public class GenericSettingPredicates {

    public static Predicate<GenericSetting> withKey(String key) {
        return (root, query, cb) -> cb.equal(root.get(GenericSetting_.key), key);
    }
}