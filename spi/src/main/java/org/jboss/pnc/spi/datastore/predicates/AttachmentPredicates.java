/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.datastore.predicates;

import java.util.Optional;

import org.jboss.pnc.model.Attachment;
import org.jboss.pnc.model.Attachment_;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildRecord_;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;

public class AttachmentPredicates {
    public static Predicate<Attachment> withSha256(Optional<String> sha256) {
        return ((root, query, cb) -> sha256.isPresent() ? cb.equal(root.get(Attachment_.sha256), sha256.get())
                : cb.and());
    }

    public static Predicate<Attachment> withBuildRecordId(Base32LongID buildRecordId) {
        return (root, query, cb) -> cb.equal(root.join(Attachment_.buildRecord).get(BuildRecord_.id), buildRecordId);
    }
}
