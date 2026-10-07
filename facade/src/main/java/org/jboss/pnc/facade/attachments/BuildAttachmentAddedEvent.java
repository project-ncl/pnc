/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.attachments;

import org.jboss.pnc.dto.Attachment;

public interface BuildAttachmentAddedEvent {

    Attachment getNewAttachment();
}
