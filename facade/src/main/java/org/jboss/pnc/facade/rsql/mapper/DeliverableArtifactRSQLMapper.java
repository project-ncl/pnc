/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.rsql.mapper;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.jboss.pnc.model.DeliverableArtifact;
import org.jboss.pnc.model.DeliverableArtifactPK;
import org.jboss.pnc.model.DeliverableArtifact_;
import org.jboss.pnc.model.GenericEntity;

@ApplicationScoped
public class DeliverableArtifactRSQLMapper extends AbstractRSQLMapper<DeliverableArtifactPK, DeliverableArtifact> {

    public DeliverableArtifactRSQLMapper() {
        super(DeliverableArtifact.class);
    }

    @Override
    protected SingularAttribute<? super DeliverableArtifact, ? extends GenericEntity<?>> toEntity(String name) {
        switch (name) {
            case "artifact":
                return DeliverableArtifact_.artifact;
            default:
                return null;
        }
    }

    @Override
    protected SetAttribute<DeliverableArtifact, ? extends GenericEntity<?>> toEntitySet(String name) {
        return null;
    }

    @Override
    protected SingularAttribute<? super DeliverableArtifact, ?> toAttribute(String name) {
        switch (name) {
            case "brewId":
                return DeliverableArtifact_.brewBuildId;
            case "builtFromSource":
                return DeliverableArtifact_.builtFromSource;
            default:
                return null;
        }
    }
}
