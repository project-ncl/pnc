/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.rsql.mapper;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;

import org.jboss.pnc.facade.rsql.RSQLException;
import org.jboss.pnc.facade.rsql.RSQLSelectorPath;
import org.jboss.pnc.facade.rsql.converter.CastValueConverter;
import org.jboss.pnc.facade.rsql.converter.ValueConverter;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductMilestone_;
import org.jboss.pnc.model.ProductRelease_;
import org.jboss.pnc.model.ProductVersion_;
import org.jboss.pnc.model.Product_;
import org.jboss.util.NotImplementedException;

/**
 * Mapper for converting RSQL over {@link org.jboss.pnc.dto.response.MilestoneInfo} into Criteria API.
 */
@ApplicationScoped
public class MilestoneInfoRSQLMapper implements RSQLMapper<Integer, ProductMilestone> {

    private static final ValueConverter valueConverter = new CastValueConverter();

    @Override
    public Class<ProductMilestone> type() {
        return null; // will not be picked by UniversalRSQLMapper
    }

    @Override
    public Path<?> toPath(From<?, ProductMilestone> from, RSQLSelectorPath selector) {
        String selectorName = selector.getElement();
        switch (selectorName) {
            case "productName":
                return from.join(ProductMilestone_.productVersion).join(ProductVersion_.product).get(Product_.name);
            case "productVersion":
                return from.join(ProductMilestone_.productVersion).get(ProductVersion_.version);
            case "milestoneVersion":
                return from.get(ProductMilestone_.version);
            case "releaseVersion":
                return from.join(ProductMilestone_.productRelease).get(ProductRelease_.version);
            case "milestoneEndDate":
                return from.get(ProductMilestone_.endDate);
            case "releaseReleaseDate":
                return from.join(ProductMilestone_.productRelease).get(ProductRelease_.releaseDate);
            default:
                throw new RSQLException("Unknown RSQL selector " + selectorName + " for type MilestoneInfo");
        }
    }

    @Override
    public String toPath(RSQLSelectorPath selector) {
        throw new NotImplementedException();
    }

    @Override
    public ValueConverter getValueConverter(String name) {
        return valueConverter;
    }
}
