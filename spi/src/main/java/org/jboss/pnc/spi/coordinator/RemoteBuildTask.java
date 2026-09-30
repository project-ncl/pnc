/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.coordinator;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.spi.BuildOptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * A task to be sent to the Rex
 */
@AllArgsConstructor
@Getter
@ToString
public class RemoteBuildTask {

    private String id;

    private Instant submitTime;

    private BuildConfigurationAudited buildConfigurationAudited;

    private BuildOptions buildOptions;

    private String username;

    private boolean alreadyRunning;

    private Optional<BuildRecord> noRebuildCause;

    private ProductMilestone currentProductMilestone;

    private List<String> dependencies;

    private List<String> dependants;

}
