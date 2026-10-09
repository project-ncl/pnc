/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.response;

import java.util.List;
import java.util.Set;

import org.jboss.pnc.api.deliverablesanalyzer.dto.LicenseInfo;
import org.jboss.pnc.dto.Artifact;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * This DTO provides information about the artifact, which was analyzed by deliverable analyzer operation.
 */
@Value
@Builder
@Jacksonized
public class AnalyzedArtifact {

    /**
     * Flag describing whether this artifact was built in some build system, e.g. PNC, Brew.
     */
    boolean builtFromSource;

    /**
     * The ID of the Brew build (in case the artifact was built in the Brew) in which was built this artifact.
     */
    Long brewId;

    /**
     * Artifact's actual data.
     */
    Artifact artifact;

    /**
     * The list of archive filenames associated with this artifact
     */
    List<String> archiveFilenames;

    /**
     * The list of archive unmatched filenames inside this artifact
     */
    List<String> archiveUnmatchedFilenames;

    /**
     * The licenses identified for this artifact
     */
    Set<LicenseInfo> licenses;

    /**
     * The distribution which was analyzed and is associated with this analyzed artifact
     */
    AnalyzedDistribution distribution;
}
