/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.datastore.repositories;

import javax.ejb.Stateless;

import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.model.Attachment;
import org.jboss.pnc.spi.datastore.repositories.AttachmentRepository;

@Stateless
public class AttachmentRepositoryImpl extends AbstractRepository<Attachment, Integer> implements AttachmentRepository {

    /**
     * @deprecated Created for CDI.
     */
    @Deprecated
    public AttachmentRepositoryImpl() {
        super(Attachment.class, Integer.class);
    }

}
