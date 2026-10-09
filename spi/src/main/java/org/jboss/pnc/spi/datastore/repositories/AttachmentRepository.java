/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.datastore.repositories;

import org.jboss.pnc.model.Attachment;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;

public interface AttachmentRepository extends Repository<Attachment, Integer> {

}
