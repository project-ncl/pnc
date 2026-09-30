/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.rsql.mapper;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.Project_;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@ApplicationScoped
public class ProjectRSQLMapper extends AbstractRSQLMapper<Integer, Project> {

    public ProjectRSQLMapper() {
        super(Project.class);
    }

    @Override
    protected SingularAttribute<Project, ? extends GenericEntity<Integer>> toEntity(String name) {
        switch (name) {
            default:
                return null;
        }
    }

    @Override
    protected SetAttribute<Project, ? extends GenericEntity<?>> toEntitySet(String name) {
        return null;
    }

    @Override
    protected SingularAttribute<Project, ?> toAttribute(String name) {
        switch (name) {
            case "id":
                return Project_.id;
            case "name":
                return Project_.name;
            case "description":
                return Project_.description;
            case "issueTrackerUrl":
                return Project_.issueTrackerUrl;
            case "projectUrl":
                return Project_.projectUrl;
            default:
                return null;
        }
    }

}
