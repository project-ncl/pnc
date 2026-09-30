/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.rsql.mapper;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfigSetRecord_;
import org.jboss.pnc.model.GenericEntity;

/**
 *
 * @author <a href="mailto:jmichalo@redhat.com">Jan Michalov</a>
 */
@ApplicationScoped
public class GroupBuildRSQLMapper extends AbstractRSQLMapper<Base32LongID, BuildConfigSetRecord> {

    public GroupBuildRSQLMapper() {
        super(BuildConfigSetRecord.class);
    }

    @Override
    protected SingularAttribute<BuildConfigSetRecord, ? extends GenericEntity<Integer>> toEntity(String name) {
        switch (name) {
            case "user":
                return BuildConfigSetRecord_.user;
            case "groupConfig":
                return BuildConfigSetRecord_.buildConfigurationSet;
            case "productVersion":
                return BuildConfigSetRecord_.productVersion;
            default:
                return null;
        }
    }

    @Override
    protected SetAttribute<BuildConfigSetRecord, ? extends GenericEntity<?>> toEntitySet(String name) {
        return null;
    }

    @Override
    protected SingularAttribute<BuildConfigSetRecord, ?> toAttribute(String name) {
        switch (name) {
            case "id":
                return BuildConfigSetRecord_.id;
            case "startTime":
                return BuildConfigSetRecord_.startTime;
            case "endTime":
                return BuildConfigSetRecord_.endTime;
            case "status":
                return BuildConfigSetRecord_.status;
            case "temporaryBuild":
                return BuildConfigSetRecord_.temporaryBuild;
            default:
                return null;
        }
    }
}
