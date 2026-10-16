/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.rsql.mapper;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfiguration_;
import org.jboss.pnc.model.GenericEntity;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@ApplicationScoped
public class BuildConfigurationRSQLMapper extends AbstractRSQLMapper<Integer, BuildConfiguration> {

    public BuildConfigurationRSQLMapper() {
        super(BuildConfiguration.class);
    }

    @Override
    protected SingularAttribute<BuildConfiguration, ? extends GenericEntity<Integer>> toEntity(String name) {
        switch (name) {
            case "project":
                return BuildConfiguration_.project;
            case "scmRepository":
                return BuildConfiguration_.repositoryConfiguration;
            case "environment":
                return BuildConfiguration_.buildEnvironment;
            case "productVersion":
                return BuildConfiguration_.productVersion;
            default:
                return null;
        }
    }

    @Override
    protected SetAttribute<BuildConfiguration, ? extends GenericEntity<?>> toEntitySet(String name) {
        switch (name) {
            case "groupConfigurations":
                return BuildConfiguration_.buildConfigurationSets;
            default:
                return null;
        }
    }

    @Override
    protected SingularAttribute<BuildConfiguration, ?> toAttribute(String name) {
        switch (name) {
            case "id":
                return BuildConfiguration_.id;
            case "name":
                return BuildConfiguration_.name;
            case "description":
                return BuildConfiguration_.description;
            case "buildScript":
                return BuildConfiguration_.buildScript;
            case "scmRevision":
                return BuildConfiguration_.scmRevision;
            case "creationTime":
                return BuildConfiguration_.creationTime;
            case "modificationTime":
                return BuildConfiguration_.lastModificationTime;
            case "buildType":
                return BuildConfiguration_.buildType;
            default:
                return null;
        }
    }
}
