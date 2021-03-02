/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.model.BuildEnvironment_;
import org.jboss.pnc.model.GenericEntity;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

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
