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

import org.jboss.pnc.facade.rsql.RSQLSelectorPath;
import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.model.ProductMilestone_;
import org.jboss.pnc.model.ProductRelease;
import org.jboss.pnc.model.ProductRelease_;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@ApplicationScoped
public class ProductReleaseRSQLMapper extends AbstractRSQLMapper<Integer, ProductRelease> {

    @Inject
    private ProductVersionRSQLMapper pvm;

    public ProductReleaseRSQLMapper() {
        super(ProductRelease.class);
    }

    @Override
    public Path<?> toPath(From<?, ProductRelease> from, RSQLSelectorPath selector) {
        switch (selector.getElement()) {
            case "productVersion":
                return pvm.toPath(
                        from.join(ProductRelease_.productMilestone).join(ProductMilestone_.productVersion),
                        selector.next());
            default:
                return super.toPath(from, selector);
        }
    }

    @Override
    public String toPath(RSQLSelectorPath selector) {
        switch (selector.getElement()) {
            case "productVersion":
                return ProductRelease_.productMilestone.getName() + '.' + ProductMilestone_.productVersion + '.'
                        + pvm.toPath(selector);
            default:
                return super.toPath(selector);
        }
    }

    @Override
    protected SingularAttribute<ProductRelease, ? extends GenericEntity<Integer>> toEntity(String name) {
        switch (name) {
            case "productMilestone":
                return ProductRelease_.productMilestone;
            default:
                return null;
        }
    }

    @Override
    protected SetAttribute<ProductRelease, ? extends GenericEntity<?>> toEntitySet(String name) {
        return null;
    }

    @Override
    protected SingularAttribute<ProductRelease, ?> toAttribute(String name) {
        switch (name) {
            case "id":
                return ProductRelease_.id;
            case "version":
                return ProductRelease_.version;
            case "supportLevel":
                return ProductRelease_.supportLevel;
            case "releaseDate":
                return ProductRelease_.releaseDate;
            default:
                return null;
        }
    }

}
