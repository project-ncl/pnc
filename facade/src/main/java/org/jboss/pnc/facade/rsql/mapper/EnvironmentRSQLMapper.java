/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.rsql.mapper;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.model.BuildEnvironment_;
import org.jboss.pnc.model.GenericEntity;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@ApplicationScoped
public class EnvironmentRSQLMapper extends AbstractRSQLMapper<Integer, BuildEnvironment> {

    public EnvironmentRSQLMapper() {
        super(BuildEnvironment.class);
    }

    @Override
    protected SingularAttribute<BuildEnvironment, ? extends GenericEntity<Integer>> toEntity(String name) {
        switch (name) {
            default:
                return null;
        }
    }

    @Override
    protected SetAttribute<BuildEnvironment, ? extends GenericEntity<?>> toEntitySet(String name) {
        return null;
    }

    @Override
    protected SingularAttribute<BuildEnvironment, ?> toAttribute(String name) {
        switch (name) {
            case "id":
                return BuildEnvironment_.id;
            case "name":
                return BuildEnvironment_.name;
            case "description":
                return BuildEnvironment_.description;
            case "systemImageRepositoryUrl":
                return BuildEnvironment_.systemImageRepositoryUrl;
            case "systemImageId":
                return BuildEnvironment_.systemImageId;
            case "systemImageType":
                return BuildEnvironment_.systemImageType;
            case "deprecated":
                return BuildEnvironment_.deprecated;
            case "hidden":
                return BuildEnvironment_.hidden;
            default:
                return null;
        }
    }

}
