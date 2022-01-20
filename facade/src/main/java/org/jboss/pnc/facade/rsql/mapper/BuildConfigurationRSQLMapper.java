/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.facade.rsql.mapper;

import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfiguration_;
import org.jboss.pnc.model.GenericEntity;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

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
