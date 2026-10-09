/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.rsql.mapper;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildConfigurationSet_;
import org.jboss.pnc.model.GenericEntity;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@ApplicationScoped
public class GroupConfigurationRSQLMapper extends AbstractRSQLMapper<Integer, BuildConfigurationSet> {

    public GroupConfigurationRSQLMapper() {
        super(BuildConfigurationSet.class);
    }

    @Override
    protected SingularAttribute<BuildConfigurationSet, ? extends GenericEntity<Integer>> toEntity(String name) {
        switch (name) {
            case "productVersion":
                return BuildConfigurationSet_.productVersion;
            default:
                return null;
        }
    }

    @Override
    protected SetAttribute<BuildConfigurationSet, ? extends GenericEntity<?>> toEntitySet(String name) {
        return null;
    }

    @Override
    protected SingularAttribute<BuildConfigurationSet, ?> toAttribute(String name) {
        switch (name) {
            case "id":
                return BuildConfigurationSet_.id;
            case "name":
                return BuildConfigurationSet_.name;
            default:
                return null;
        }
    }

}
