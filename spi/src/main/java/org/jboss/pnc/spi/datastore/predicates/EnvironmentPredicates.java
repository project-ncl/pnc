/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.datastore.predicates;

import javax.persistence.criteria.MapJoin;

import org.jboss.pnc.api.constants.Attributes;
import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.model.BuildEnvironment_;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;

/**
 * Predicates for {@link org.jboss.pnc.model.BuildEnvironment} entity.
 */
public class EnvironmentPredicates {

    public static Predicate<BuildEnvironment> withNotHidden() {
        return (root, query, cb) -> cb.equal(root.get(BuildEnvironment_.hidden), false);
    }

    public static Predicate<BuildEnvironment> withEnvironmentName(String name) {
        return (root, query, cb) -> cb.equal(root.get(BuildEnvironment_.name), name);
    }

    public static Predicate<BuildEnvironment> withEnvironmentNameAndActive(String name) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get(BuildEnvironment_.name), name),
                cb.equal(root.get(BuildEnvironment_.deprecated), false));
    }

    public static Predicate<BuildEnvironment> replacedBy(String replacementId) {
        return (root, query, cb) -> {
            MapJoin<BuildEnvironment, String, String> join = root.join(BuildEnvironment_.attributes);
            return cb.and(
                    cb.isTrue(root.get(BuildEnvironment_.deprecated)),
                    cb.equal(join.key(), Attributes.DEPRECATION_REPLACEMENT),
                    cb.equal(join.value(), replacementId));
        };
    }

}
