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
import org.jboss.pnc.model.TargetRepository;
import org.jboss.pnc.model.TargetRepository_;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

/**
 * @author <a href="mailto:jmichalo@redhat.com">Jan Michalov</a>
 */
@ApplicationScoped
public class TargetRepositoryRSQLMapper extends AbstractRSQLMapper<Integer, TargetRepository> {

    public TargetRepositoryRSQLMapper() {
        super(TargetRepository.class);
    }

    @Override
    protected SingularAttribute<TargetRepository, ? extends GenericEntity<Integer>> toEntity(String name) {
        switch (name) {
            default:
                return null;
        }
    }

    @Override
    protected SetAttribute<TargetRepository, ? extends GenericEntity<?>> toEntitySet(String name) {
        return null;
    }

    @Override
    protected SingularAttribute<TargetRepository, ?> toAttribute(String name) {
        switch (name) {
            case "id":
                return TargetRepository_.id;
            case "repositoryPath":
                return TargetRepository_.repositoryPath;
            case "repositoryType":
                return TargetRepository_.repositoryType;
            case "identifier":
                return TargetRepository_.identifier;
            case "temporaryRepo":
                return TargetRepository_.temporaryRepo;
            default:
                return null;
        }
    }
}
