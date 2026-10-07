/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mock.repository;

import org.jboss.pnc.model.Attachment;
import org.jboss.pnc.spi.datastore.repositories.AttachmentRepository;

public class AttachmentRepositoryMock extends IntIdRepositoryMock<Attachment> implements AttachmentRepository {
}
