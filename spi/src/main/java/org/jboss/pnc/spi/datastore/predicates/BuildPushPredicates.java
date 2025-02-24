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

import org.apache.commons.collections.CollectionUtils;
import org.jboss.pnc.model.Artifact_;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildPushOperation;
import org.jboss.pnc.model.BuildPushOperation_;
import org.jboss.pnc.model.BuildRecord_;
import org.jboss.pnc.model.DeliverableAnalyzerDistribution;
import org.jboss.pnc.model.DeliverableAnalyzerDistribution_;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;

import java.util.Set;

/**
 * Predicates for {@link org.jboss.pnc.model.BuildPushOperation} and {@link org.jboss.pnc.model.BuildPushReport}
 * entities.
 */
public class BuildPushPredicates {

    public static Predicate<BuildPushOperation> withBuild(Base32LongID buildId) {
        return (root, query, cb) -> cb.equal(root.get(BuildPushOperation_.build).get(BuildRecord_.id), buildId);
    }

    public static Predicate<BuildPushOperation> withBuilds(Set<Base32LongID> buildIds) {
        if (CollectionUtils.isEmpty(buildIds)) {
            return Predicate.nonMatching();
        } else {
            return (root, query, cb) -> root.get(BuildPushOperation_.build).get(BuildRecord_.id).in(buildIds);
        }
    }
}
