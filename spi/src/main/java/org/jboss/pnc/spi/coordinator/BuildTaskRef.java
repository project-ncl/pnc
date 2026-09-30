/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.coordinator;

import java.time.Instant;
import java.util.Set;

import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.api.enums.RebuildMode;
import org.jboss.pnc.enums.BuildCoordinationStatus;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.User;

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

    Base32LongID getBuildConfigSetRecordId();

    ProductMilestone getProductMilestone();

    User getUser();

    BuildCoordinationStatus getStatus();

    boolean isTemporaryBuild();

    AlignmentPreference getAlignmentPreference();

    RebuildMode getRebuildMode();

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
