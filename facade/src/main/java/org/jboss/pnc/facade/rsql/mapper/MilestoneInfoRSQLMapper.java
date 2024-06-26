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

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;

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
