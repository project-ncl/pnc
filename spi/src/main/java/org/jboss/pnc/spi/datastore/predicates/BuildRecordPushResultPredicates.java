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
package org.jboss.pnc.spi.datastore.predicates;

import org.jboss.pnc.enums.BuildPushStatus;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildRecordPushResult;
import org.jboss.pnc.model.BuildRecordPushResult_;
import org.jboss.pnc.model.BuildRecord_;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildRecordPushResultPredicates {

    public static Predicate<BuildRecordPushResult> forBuildRecordOrderByIdDesc(Base32LongID buildRecordId) {
        return (root, query, cb) -> {
            query.orderBy(cb.desc(root.get(BuildRecordPushResult_.id)));
            return cb.equal(root.get(BuildRecordPushResult_.buildRecord).get(BuildRecord_.id), buildRecordId);
        };
    }

    public static Predicate<BuildRecordPushResult> successForBuildRecord(Base32LongID buildRecordId) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get(BuildRecordPushResult_.buildRecord).get(BuildRecord_.id), buildRecordId),
                cb.equal(root.get(BuildRecordPushResult_.status), BuildPushStatus.SUCCESS));
    }

}
