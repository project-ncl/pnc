/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.datastore.predicates;

import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.Project_;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;

/**
 * Predicates for {@link org.jboss.pnc.model.Project} entity.
 */
public class ProjectPredicates {

    public static Predicate<Project> withProjectId(Integer projectId) {
        return (root, query, cb) -> cb.equal(root.get(Project_.id), projectId);
    }

    public static Predicate<Project> withProjectName(String name) {
        return (root, query, cb) -> cb.equal(root.get(Project_.name), name);
    }

    public static Predicate<Project> searchByProjectName(String name) {
        return (root, query, cb) -> cb.like(root.get(Project_.name), name);
    }

}
