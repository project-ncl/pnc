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
package org.jboss.pnc.spi.datastore.repositories;

import org.jboss.pnc.model.DeliverableAnalyzerReport;
import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;

import java.io.Serializable;

/**
 *
 * @param <LO_ID>, e.g. id of {@link DeliverableAnalyzerReport}
 * @param <LH_ID>
 * @param <LH>, e.g. {@link org.jboss.pnc.model.DeliverableAnalyzerLabelEntry}
 */
public interface LabelEntryRepository<LO_ID extends Serializable, LH_ID extends Serializable, LH extends GenericEntity<LH_ID>>
        extends Repository<LH, LH_ID> {

    /**
     * Gets the latest changeOrder for the labeled object, given by its id
     *
     * @param labeledObjectId the ID of the labeled object
     * @return latest changeOrder
     */
    Integer getLatestChangeOrderOfReport(LO_ID labeledObjectId);
}
