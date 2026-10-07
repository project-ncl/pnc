/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.attachments;

import org.jboss.pnc.dto.Attachment;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class DefaultBuildAttachmentAddedEvent implements BuildAttachmentAddedEvent {

    private final Attachment attachment;

    public DefaultBuildAttachmentAddedEvent(Attachment attachment) {
        this.attachment = attachment;
    }

    @Override
    public Attachment getNewAttachment() {
        return attachment;
    }
}
