/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * The composite primary key of the {@link DeliverableArtifact} table.
 *
 * @author Adam Kridl &lt;akridl@redhat.com&gt;
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class DeliverableArtifactPK implements Serializable {

    /**
     * The id of the report.
     */
    private DeliverableAnalyzerReport report;

    /**
     * The id of the artifact.
     */
    private Artifact artifact;

    /**
     * The id of distribution
     */
    private DeliverableAnalyzerDistribution distribution;
}
