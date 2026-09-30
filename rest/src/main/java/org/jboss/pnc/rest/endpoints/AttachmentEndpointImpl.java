/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest.endpoints;

import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.pnc.dto.Attachment;
import org.jboss.pnc.dto.AttachmentRef;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.providers.api.AttachmentProvider;
import org.jboss.pnc.facade.util.UserService;
import org.jboss.pnc.rest.api.endpoints.AttachmentEndpoint;
import org.jboss.pnc.rest.api.parameters.PageParameters;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class AttachmentEndpointImpl implements AttachmentEndpoint {

    private EndpointHelper<Integer, Attachment, AttachmentRef> endpointHelper;

    @Inject
    private AttachmentProvider attachmentProvider;

    @Inject
    private UserService userService;

    @PostConstruct
    public void init() {
        endpointHelper = new EndpointHelper<>(Attachment.class, attachmentProvider);
    }

    @Override
    public Page<Attachment> getAll(PageParameters pageParams, String sha256) {
        return attachmentProvider.getAll(
                pageParams.getPageIndex(),
                pageParams.getPageSize(),
                pageParams.getSort(),
                pageParams.getQ(),
                Optional.ofNullable(sha256));
    }

    @Override
    public Attachment getSpecific(String id) {
        return endpointHelper.getSpecific(id);
    }

    @Override
    public Attachment create(Attachment attachment) {
        return endpointHelper.create(attachment);
    }

    @Override
    public void update(String id, Attachment attachment) {
        endpointHelper.update(id, attachment);
    }
}
