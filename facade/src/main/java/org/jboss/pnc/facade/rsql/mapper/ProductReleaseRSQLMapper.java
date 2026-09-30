/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.rsql.mapper;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.jboss.pnc.facade.rsql.RSQLSelectorPath;
import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.model.ProductMilestone_;
import org.jboss.pnc.model.ProductRelease;
import org.jboss.pnc.model.ProductRelease_;

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
