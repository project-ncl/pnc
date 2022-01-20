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
import org.jboss.pnc.model.ProductMilestoneRelease;
import org.jboss.pnc.model.ProductMilestoneRelease_;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@ApplicationScoped
public class ProductMilestoneReleaseRSQLMapper extends AbstractRSQLMapper<Long, ProductMilestoneRelease> {

    public ProductMilestoneReleaseRSQLMapper() {
        super(ProductMilestoneRelease.class);
    }

    @Override
    protected SingularAttribute<ProductMilestoneRelease, ? extends GenericEntity<Integer>> toEntity(String name) {
        switch (name) {
            case "milestone":
                return ProductMilestoneRelease_.milestone;
            default:
                return null;
        }
    }

    @Override
    protected SetAttribute<ProductMilestoneRelease, ? extends GenericEntity<?>> toEntitySet(String name) {
        return null;
    }

    @Override
    protected SingularAttribute<ProductMilestoneRelease, ?> toAttribute(String name) {
        switch (name) {
            case "id":
                return ProductMilestoneRelease_.id;
            case "status":
                return ProductMilestoneRelease_.status;
            case "endDate":
                return ProductMilestoneRelease_.endDate;
            case "startingDate":
                return ProductMilestoneRelease_.startingDate;
            default:
                return null;
        }
    }

}
