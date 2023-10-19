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
package org.jboss.pnc.facade.util.labels;

import org.jboss.pnc.model.GenericEntity;

import java.io.Serializable;

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
