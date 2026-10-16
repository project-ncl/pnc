/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.datastore.repositories;

import javax.ejb.Stateless;

import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.spi.datastore.repositories.ProjectRepository;

@Stateless
public class ProjectRepositoryImpl extends AbstractRepository<Project, Integer> implements ProjectRepository {

    public ProjectRepositoryImpl() {
        super(Project.class, Integer.class);
    }
}
