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
package org.jboss.pnc.facade.providers.api;

import org.jboss.pnc.dto.Attachment;
import org.jboss.pnc.dto.AttachmentRef;
import org.jboss.pnc.dto.response.Page;

import java.util.Optional;

public interface AttachmentProvider
        extends Provider<Integer, org.jboss.pnc.model.Attachment, Attachment, AttachmentRef> {
    Page<Attachment> getAll(int pageIndex, int pageSize, String sortingRsql, String query, Optional<String> md5);

    Page<Attachment> getAttachmentsForBuild(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            String buildId);
}
