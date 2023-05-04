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
package org.jboss.pnc.spi.coordinator;

import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.enums.BuildCoordinationStatus;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.User;

import java.time.Instant;
import java.util.Set;

/**
 * Representing remote running task.
 */
public interface BuildTaskRef {
    String getId();

    IdRev getIdRev();

    String getContentId();

    Instant getSubmitTime();

    Instant getStartTime();

    Instant getEndTime();

    Long getBuildConfigSetRecordId();

    ProductMilestone getProductMilestone();

    User getUser();

    BuildCoordinationStatus getStatus();

    boolean isTemporaryBuild();

    AlignmentPreference getAlignmentPreference();

    BuildRecord getNoRebuildCause();

    /**
     * Build dependants of this Build in Orch. This list also includes Builds that were not scheduled like NRR Builds.
     * <p>
     * The list consists of BuildRecord IDs
     * <p>
     * 
     * @return Ser of Build dependants
     */
    Set<String> getDependants();

    /**
     * Build dependencies of this Build in Orch. This List also includes Builds that were not scheduled like NRR Builds.
     * <p>
     * The list consists of BuildRecord IDs
     * <p>
     * 
     * @return Set of Build dependencies
     */
    Set<String> getDependencies();

    /**
     * Build dependencies of this Build in Rex. This List doesn't include Builds that were not scheduled like NRR
     * Builds.
     * <p>
     * The list consists of BuildRecord IDs. The List is often identical to getDependants List.
     * <p>
     * 
     * @return Set of scheduled Build dependants
     */
    Set<String> getTaskDependants();

    /**
     * Build dependencies of this Build in Rex. This List doesn't include Builds that were not scheduled like NRR
     * Builds.
     * <p>
     * The list consists of BuildRecord IDs. The List is often identical to getDependencies List.
     * <p>
     * 
     * @return Set of scheduled Build dependants
     */
    Set<String> getTaskDependencies();

}
