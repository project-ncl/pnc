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
package org.jboss.pnc.facade.providers;

import lombok.extern.slf4j.Slf4j;
import org.jboss.pnc.dto.Attachment;
import org.jboss.pnc.dto.AttachmentRef;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.attachments.BuildAttachmentAddedEvent;
import org.jboss.pnc.facade.attachments.DefaultBuildAttachmentAddedEvent;
import org.jboss.pnc.facade.providers.api.AttachmentProvider;
import org.jboss.pnc.facade.validation.DTOValidationException;
import org.jboss.pnc.mapper.api.AttachmentMapper;
import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.spi.datastore.repositories.AttachmentRepository;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.Optional;

import static org.jboss.pnc.facade.providers.api.UserRoles.USERS_ADMIN;
import static org.jboss.pnc.facade.providers.api.UserRoles.USERS_ATTACHMENT_ADMIN;
import static org.jboss.pnc.spi.datastore.predicates.AttachmentPredicates.withBuildRecordId;
import static org.jboss.pnc.spi.datastore.predicates.AttachmentPredicates.withMd5;

@PermitAll
@Stateless
@Slf4j
public class AttachmentProviderImpl
        extends AbstractUpdatableProvider<Integer, org.jboss.pnc.model.Attachment, Attachment, AttachmentRef>
        implements AttachmentProvider {

    private final Event<BuildAttachmentAddedEvent> event;

    @Inject
    public AttachmentProviderImpl(
            AttachmentRepository repository,
            AttachmentMapper mapper,
            Event<BuildAttachmentAddedEvent> event) {
        super(repository, mapper, org.jboss.pnc.model.Attachment.class);
        this.event = event;
    }

    @Override
    public Page<Attachment> getAll(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            Optional<String> md5) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withMd5(md5));
    }

    @Override
    public Page<Attachment> getAttachmentsForBuild(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            String buildId) {
        return queryForCollection(
                pageIndex,
                pageSize,
                sortingRsql,
                query,
                withBuildRecordId(BuildMapper.idMapper.toEntity(buildId)));
    }

    @Override
    @RolesAllowed({ USERS_ATTACHMENT_ADMIN, USERS_ADMIN })
    public Attachment store(Attachment restEntity) throws DTOValidationException {
        Attachment saved = super.store(restEntity);

        // notify listeners that new attachment appeared (could be a result of analysis)
        event.fireAsync(new DefaultBuildAttachmentAddedEvent(saved));

        return saved;
    }

    @Override
    @RolesAllowed({ USERS_ATTACHMENT_ADMIN, USERS_ADMIN })
    public Attachment update(String stringId, Attachment restEntity) {
        return super.update(stringId, restEntity);
    }
}
