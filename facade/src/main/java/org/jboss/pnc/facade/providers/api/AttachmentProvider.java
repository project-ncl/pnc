/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.providers.api;

import java.util.Optional;

import org.jboss.pnc.dto.Attachment;
import org.jboss.pnc.dto.AttachmentRef;
import org.jboss.pnc.dto.response.Page;

public interface AttachmentProvider
        extends Provider<Integer, org.jboss.pnc.model.Attachment, Attachment, AttachmentRef> {
    Page<Attachment> getAll(int pageIndex, int pageSize, String sortingRsql, String query, Optional<String> sha256);

    Page<Attachment> getAttachmentsForBuild(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            String buildId);
}
