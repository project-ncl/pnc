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
package org.jboss.pnc.indyrepositorymanager;

import org.commonjava.indy.folo.dto.TrackedContentEntryDTO;
import org.commonjava.indy.model.core.StoreKey;

public interface ArtifactFilter {

    /**
     * Checks if the artifact should be promoted or not.
     *
     * @param artifact the audited artifact
     * @param download flag if it is download or upload
     * @return true if the artifact should be part of promotion, false otherwise
     */
    boolean acceptsForPromotion(TrackedContentEntryDTO artifact, boolean download);

    /**
     * Checks if the artifact should be stored in the database.
     *
     * @param artifact the audited artifact
     * @return true if it should be stored, false otherwise
     */
    boolean acceptsForData(TrackedContentEntryDTO artifact);

    /**
     * Checks if given store is ignored for dependencies promotion.
     *
     * @param storeKey evaluated store key
     * @return true if the given store is ignored, false otherwise
     */
    boolean ignoreDependencySource(StoreKey storeKey);

}
