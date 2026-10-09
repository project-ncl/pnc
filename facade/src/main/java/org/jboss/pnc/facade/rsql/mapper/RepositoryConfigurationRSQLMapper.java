/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.rsql.mapper;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.model.RepositoryConfiguration;
import org.jboss.pnc.model.RepositoryConfiguration_;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@ApplicationScoped
public class RepositoryConfigurationRSQLMapper extends AbstractRSQLMapper<Integer, RepositoryConfiguration> {

    public RepositoryConfigurationRSQLMapper() {
        super(RepositoryConfiguration.class);
    }

    @Override
    protected SingularAttribute<RepositoryConfiguration, ? extends GenericEntity<Integer>> toEntity(String name) {
        switch (name) {
            default:
                return null;
        }
    }

    @Override
    protected SetAttribute<RepositoryConfiguration, ? extends GenericEntity<?>> toEntitySet(String name) {
        return null;
    }

    @Override
    protected SingularAttribute<RepositoryConfiguration, ?> toAttribute(String name) {
        switch (name) {
            case "id":
                return RepositoryConfiguration_.id;
            case "internalUrl":
                return RepositoryConfiguration_.internalUrl;
            case "externalUrl":
                return RepositoryConfiguration_.externalUrl;
            case "preBuildSyncEnabled":
                return RepositoryConfiguration_.preBuildSyncEnabled;
            default:
                return null;
        }
    }

}
