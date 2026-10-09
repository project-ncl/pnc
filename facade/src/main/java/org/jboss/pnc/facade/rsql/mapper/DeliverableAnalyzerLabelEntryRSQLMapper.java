/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.rsql.mapper;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.jboss.pnc.model.*;

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
