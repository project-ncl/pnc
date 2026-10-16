/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.datastore.repositories;

import javax.ejb.Stateless;

import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.spi.datastore.repositories.BuildEnvironmentRepository;

@Stateless
public class BuildEnvironmentImpl extends AbstractRepository<BuildEnvironment, Integer>
        implements BuildEnvironmentRepository {

    public BuildEnvironmentImpl() {
        super(BuildEnvironment.class, Integer.class);
    }
}
