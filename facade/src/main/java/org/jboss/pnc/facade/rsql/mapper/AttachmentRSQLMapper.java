/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.rsql.mapper;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.jboss.pnc.model.Attachment;
import org.jboss.pnc.model.Attachment_;
import org.jboss.pnc.model.GenericEntity;

/**
 * @author <a href="mailto:jmichalo@redhat.com">Jan Michalov</a>
 */
@ApplicationScoped
public class AttachmentRSQLMapper extends AbstractRSQLMapper<Integer, Attachment> {

    public AttachmentRSQLMapper() {
        super(Attachment.class);
    }

    @Override
    protected SingularAttribute<? super Attachment, ? extends GenericEntity<?>> toEntity(String name) {
        switch (name) {
            case "build":
                return Attachment_.buildRecord;
            default:
                return null;
        }
    }

    @Override
    protected SetAttribute<Attachment, ? extends GenericEntity<?>> toEntitySet(String name) {
        return null;
    }

    @Override
    protected SingularAttribute<? super Attachment, ?> toAttribute(String name) {
        switch (name) {
            case "id":
                return Attachment_.id;
            case "name":
                return Attachment_.name;
            case "sha256":
                return Attachment_.sha256;
            case "type":
                return Attachment_.type;
            case "url":
                return Attachment_.url;
            case "creationTime":
                return Attachment_.creationTime;
            default:
                return null;
        }
    }
}
