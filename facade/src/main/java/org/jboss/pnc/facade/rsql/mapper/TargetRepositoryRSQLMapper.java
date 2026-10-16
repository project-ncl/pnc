/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.rsql.mapper;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.model.TargetRepository;
import org.jboss.pnc.model.TargetRepository_;

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
