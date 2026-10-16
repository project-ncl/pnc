/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.providers;

import static org.jboss.pnc.facade.providers.api.UserRoles.USERS_ADMIN;
import static org.jboss.pnc.facade.providers.api.UserRoles.USERS_ATTACHMENT_ADMIN;
import static org.jboss.pnc.facade.providers.api.UserRoles.USERS_REX;
import static org.jboss.pnc.spi.datastore.predicates.AttachmentPredicates.withBuildRecordId;
import static org.jboss.pnc.spi.datastore.predicates.AttachmentPredicates.withSha256;

import java.util.Optional;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;

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

import lombok.extern.slf4j.Slf4j;

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
            Optional<String> sha256) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withSha256(sha256));
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
    @RolesAllowed({ USERS_ATTACHMENT_ADMIN, USERS_REX, USERS_ADMIN })
    public Attachment store(Attachment restEntity) throws DTOValidationException {
        Attachment saved = super.store(restEntity);

        // notify listeners that new attachment appeared (could be a result of analysis)
        event.fireAsync(new DefaultBuildAttachmentAddedEvent(saved));

        return saved;
    }

    @Override
    @RolesAllowed({ USERS_ATTACHMENT_ADMIN, USERS_REX, USERS_ADMIN })
    public Attachment update(String stringId, Attachment restEntity) {
        return super.update(stringId, restEntity);
    }
}
