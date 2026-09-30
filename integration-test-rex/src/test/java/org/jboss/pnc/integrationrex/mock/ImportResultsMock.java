/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.integrationrex.mock;

import static java.time.temporal.ChronoUnit.MINUTES;
import static java.util.Date.from;

import java.time.Instant;
import java.util.Map;

import org.jboss.pnc.api.enums.orch.CompletionStatus;
import org.jboss.pnc.common.Random;
import org.jboss.pnc.dto.internal.BuildDriverResultRest;
import org.jboss.pnc.dto.internal.BuildExecutionConfigurationRest;
import org.jboss.pnc.dto.internal.BuildImport;
import org.jboss.pnc.dto.internal.BuildMeta;
import org.jboss.pnc.dto.internal.BuildResultRest;
import org.jboss.pnc.dto.internal.EnvironmentDriverResultRest;
import org.jboss.pnc.dto.internal.IdRev;
import org.jboss.pnc.dto.internal.RepourResultRest;
import org.jboss.pnc.enums.BuildStatus;

public class ImportResultsMock {

    public static BuildImport generateBuildImport(
            IdRev bcrev,
            CompletionStatus status,
            Map<String, String> attributes) {
        var now = Instant.now();

        var meta = generateMeta(bcrev, now);
        var result = generateResult(status, attributes);

        return BuildImport.builder()
                .result(result)
                .metadata(meta)
                .startTime(from(now.minus(10, MINUTES)))
                .endTime(from(now))
                .build();
    }

    public static BuildMeta generateMeta(IdRev bcrev, Instant now) {
        return BuildMeta.builder()
                .idRev(bcrev)
                .contentId(bcrev.toString() + "contentId")
                .submitTime(from(now.minus(10, MINUTES)))
                .temporaryBuild(false)
                .username("demo-user")
                .build();
    }

    public static BuildResultRest generateResult(CompletionStatus status, Map<String, String> attributes) {
        return BuildResultRest.builder()
                .completionStatus(status)
                .buildExecutionConfiguration(generateBuildExecutionConfiguration())
                .repourResult(
                        RepourResultRest.builder()
                                .completionStatus(status)
                                .executionRootName("rootName")
                                .executionRootVersion("rootVersion")
                                .build())
                .buildDriverResult(BuildDriverResultRest.builder().buildStatus(BuildStatus.SUCCESS).build())
                .environmentDriverResult(EnvironmentDriverResultRest.builder().completionStatus(status).build())
                .repositoryManagerResult(BPMResultsMock.mockRepositoryManagerResultRest(Random.randString(8)))
                .extraAttributes(attributes)
                .build();
    }

    public static BuildResultRest generateFailedResult(CompletionStatus failedStatus, Map<String, String> attributes) {
        return BuildResultRest.builder()
                .completionStatus(failedStatus)
                .buildExecutionConfiguration(generateBuildExecutionConfiguration())
                .repourResult(
                        RepourResultRest.builder()
                                .completionStatus(failedStatus)
                                .executionRootName("rootName")
                                .executionRootVersion("rootVersion")
                                .build())
                .buildDriverResult(null)
                .environmentDriverResult(null)
                .repositoryManagerResult(null)
                .extraAttributes(attributes)
                .build();
    }

    public static BuildImport generateBuildImport(BuildMeta meta, BuildResultRest result) {
        return BuildImport.builder()
                .metadata(meta)
                .result(result)
                .startTime(from(Instant.now().minus(10, MINUTES)))
                .endTime(from(Instant.now()))
                .build();
    }

    public static BuildExecutionConfigurationRest generateBuildExecutionConfiguration() {
        return BuildExecutionConfigurationRest.newBuilder()
                .scmRepoURL("http://www.github.com")
                .scmRevision("f18de64523d5054395d82e24d4e28473a05a3880")
                .scmBuildConfigRevision("e18de64523d5054395d82e24d4e28473a05a3880")
                .scmBuildConfigRevisionInternal(false)
                .scmTag("1.0.0.redhat-1")
                .build();
    }
}
