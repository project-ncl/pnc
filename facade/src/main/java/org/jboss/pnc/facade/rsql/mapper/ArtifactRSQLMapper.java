/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.rsql.mapper;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.Artifact_;
import org.jboss.pnc.model.GenericEntity;

/**
 * @author <a href="mailto:jmichalo@redhat.com">Jan Michalov</a>
 */
@ApplicationScoped
public class ArtifactRSQLMapper extends AbstractRSQLMapper<Integer, Artifact> {

    public ArtifactRSQLMapper() {
        super(Artifact.class);
    }

    @Override
    protected SingularAttribute<Artifact, ? extends GenericEntity<?>> toEntity(String name) {
        switch (name) {
            case "targetRepository":
                return Artifact_.targetRepository;
            case "build":
                return Artifact_.buildRecord;
            default:
                return null;
        }
    }

    @Override
    protected SetAttribute<Artifact, ? extends GenericEntity<?>> toEntitySet(String name) {
        return null;
    }

    @Override
    protected SingularAttribute<Artifact, ?> toAttribute(String name) {
        switch (name) {
            case "id":
                return Artifact_.id;
            case "identifier":
                return Artifact_.identifier;
            case "purl":
                return Artifact_.purl;
            case "md5":
                return Artifact_.md5;
            case "sha1":
                return Artifact_.sha1;
            case "sha256":
                return Artifact_.sha256;
            case "filename":
                return Artifact_.filename;
            case "deployPath":
                return Artifact_.deployPath;
            case "originUrl":
                return Artifact_.originUrl;
            case "size":
                return Artifact_.size;
            case "importDate":
                return Artifact_.importDate;
            case "artifactQuality":
                return Artifact_.artifactQuality;
            case "buildCategory":
                return Artifact_.buildCategory;
            default:
                return null;
        }
    }
}
