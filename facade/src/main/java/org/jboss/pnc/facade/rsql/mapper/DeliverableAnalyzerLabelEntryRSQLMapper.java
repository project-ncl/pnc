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

import org.jboss.pnc.model.*;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

/**
 *
 * @author Patrik Korytár &lt;pkorytar@redhat.com&gt;
 */
@ApplicationScoped
public class DeliverableAnalyzerLabelEntryRSQLMapper
        extends AbstractRSQLMapper<Base32LongID, DeliverableAnalyzerLabelEntry> {

    public DeliverableAnalyzerLabelEntryRSQLMapper() {
        super(DeliverableAnalyzerLabelEntry.class);
    }

    @Override
    protected SingularAttribute<? super DeliverableAnalyzerLabelEntry, ? extends GenericEntity<?>> toEntity(
            String name) {
        switch (name) {
            case "user":
                return DeliverableAnalyzerLabelEntry_.user;
            default:
                return null;
        }
    }

    @Override
    protected SetAttribute<DeliverableAnalyzerLabelEntry, ? extends GenericEntity<?>> toEntitySet(String name) {
        return null;
    }

    @Override
    protected SingularAttribute<? super DeliverableAnalyzerLabelEntry, ?> toAttribute(String name) {
        switch (name) {
            case "label":
                return DeliverableAnalyzerLabelEntry_.label;
            case "reason":
                return DeliverableAnalyzerLabelEntry_.reason;
            case "date":
                return DeliverableAnalyzerLabelEntry_.entryTime;
            case "change":
                return DeliverableAnalyzerLabelEntry_.change;
            default:
                return null;
        }
    }
}
