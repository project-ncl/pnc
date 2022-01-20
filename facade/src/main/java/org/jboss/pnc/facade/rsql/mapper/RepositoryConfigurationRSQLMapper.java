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

import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.model.RepositoryConfiguration;
import org.jboss.pnc.model.RepositoryConfiguration_;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

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
