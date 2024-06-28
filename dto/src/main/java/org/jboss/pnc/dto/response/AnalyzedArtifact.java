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
package org.jboss.pnc.dto.response;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.Set;

import org.jboss.pnc.api.deliverablesanalyzer.dto.LicenseInfo;
import org.jboss.pnc.dto.Artifact;

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
