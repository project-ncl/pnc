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
package org.jboss.pnc.facade.rsql.mapper;

import org.jboss.pnc.model.Attachment;
import org.jboss.pnc.model.Attachment_;
import org.jboss.pnc.model.GenericEntity;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

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
            case "md5":
                return Attachment_.md5;
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
