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

import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.Artifact_;
import org.jboss.pnc.model.Attachment_;
import org.jboss.pnc.model.Attachment;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildRecord_;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;

import java.util.Optional;

public class AttachmentPredicates {
    public static Predicate<Attachment> withMd5(Optional<String> md5) {
        return ((root, query, cb) -> md5.isPresent() ? cb.equal(root.get(Attachment_.md5), md5.get()) : cb.and());
    }

    public static Predicate<Attachment> withBuildRecordId(Base32LongID buildRecordId) {
        return (root, query, cb) -> cb.equal(root.join(Attachment_.buildRecord).get(BuildRecord_.id), buildRecordId);
    }
}
