/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.util.labels;

import java.io.Serializable;

import org.jboss.pnc.model.GenericEntity;

/**
 * Gets the requests what to store and without no further validation stores into DB requested entities.
 *
 * @param <LO_ID> The id of the labeled object entity.
 * @param <L> The label enum.
 * @param <LO> The labeled object entity, e.g. {@link org.jboss.pnc.model.DeliverableAnalyzerReport}.
 */
public interface LabelSaver<LO_ID extends Serializable, L extends Enum<L>, LO extends GenericEntity<LO_ID>> {

    void init(LO labeledObject, String reason);

    void addLabel(L label);

    void removeLabel(L label);
}
