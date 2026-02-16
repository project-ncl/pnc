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
package org.jboss.pnc.rest.endpoints;

import lombok.extern.slf4j.Slf4j;
import org.jboss.pnc.dto.Attachment;
import org.jboss.pnc.dto.AttachmentRef;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.providers.api.AttachmentProvider;
import org.jboss.pnc.facade.util.UserService;
import org.jboss.pnc.model.User;
import org.jboss.pnc.rest.api.endpoints.AttachmentEndpoint;
import org.jboss.pnc.rest.api.parameters.PageParameters;

import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.util.Optional;

import static org.jboss.pnc.facade.providers.api.UserRoles.USERS_ADMIN;
import static org.jboss.pnc.facade.providers.api.UserRoles.USERS_ATTACHMENT_ADMIN;

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
    public Page<Attachment> getAll(PageParameters pageParams, String md5) {
        return attachmentProvider.getAll(
                pageParams.getPageIndex(),
                pageParams.getPageSize(),
                pageParams.getSort(),
                pageParams.getQ(),
                Optional.ofNullable(md5));
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
