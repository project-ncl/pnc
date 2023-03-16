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
package org.jboss.pnc.spi.datastore.predicates;

import org.jboss.pnc.api.constants.Attributes;
import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.model.BuildEnvironment_;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;

import javax.persistence.criteria.MapJoin;

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
