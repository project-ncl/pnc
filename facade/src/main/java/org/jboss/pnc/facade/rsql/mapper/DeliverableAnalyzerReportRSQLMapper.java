
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
import org.jboss.pnc.model.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

/**
 *
 * @author Patrik Korytár &lt;pkorytar@redhat.com&gt;
 */
@ApplicationScoped
public class DeliverableAnalyzerReportRSQLMapper extends AbstractRSQLMapper<Base32LongID, DeliverableAnalyzerReport> {

    @Inject
    private ProductMilestoneRSQLMapper productMilestoneRSQLMapper;

    @Inject
    private UserRSQLMapper userRSQLMapper;

    public DeliverableAnalyzerReportRSQLMapper() {
        super(DeliverableAnalyzerReport.class);
    }

    @Override
    public Path<?> toPath(From<?, DeliverableAnalyzerReport> from, RSQLSelectorPath selector) {
        switch (selector.getElement()) {
            case "submitTime":
                return from.join(DeliverableAnalyzerReport_.operation).get(DeliverableAnalyzerOperation_.submitTime);
            case "startTime":
                return from.join(DeliverableAnalyzerReport_.operation).get(DeliverableAnalyzerOperation_.startTime);
            case "endTime":
                return from.join(DeliverableAnalyzerReport_.operation).get(DeliverableAnalyzerOperation_.endTime);
            case "productMilestone":
                return productMilestoneRSQLMapper.toPath(
                        from.join(DeliverableAnalyzerReport_.operation)
                                .join(DeliverableAnalyzerOperation_.productMilestone),
                        selector.next());
            case "user":
                return userRSQLMapper.toPath(
                        from.join(DeliverableAnalyzerReport_.operation).join(DeliverableAnalyzerOperation_.user),
                        selector.next());
            default:
                return super.toPath(from, selector);
        }
    }

    @Override
    protected SingularAttribute<? super DeliverableAnalyzerReport, ? extends GenericEntity<?>> toEntity(String name) {
        return null;

    }

    @Override
    protected SetAttribute<DeliverableAnalyzerReport, ? extends GenericEntity<?>> toEntitySet(String name) {
        return null;
    }

    @Override
    protected SingularAttribute<? super DeliverableAnalyzerReport, ?> toAttribute(String name) {
        switch (name) {
            case "id":
                return DeliverableAnalyzerReport_.id;
            default:
                return null;
        }
    }
}