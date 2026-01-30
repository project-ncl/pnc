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
package org.jboss.pnc.integrationrex.mock;

import org.jboss.pnc.api.enums.orch.CompletionStatus;
import org.jboss.pnc.api.orch.dto.BuildDriverResultRest;
import org.jboss.pnc.api.orch.dto.BuildExecutionConfigurationRest;
import org.jboss.pnc.api.orch.dto.BuildImport;
import org.jboss.pnc.api.orch.dto.BuildMeta;
import org.jboss.pnc.api.orch.dto.BuildResultRest;
import org.jboss.pnc.api.orch.dto.EnvironmentDriverResultRest;
import org.jboss.pnc.api.orch.dto.IdRev;
import org.jboss.pnc.api.orch.dto.RepourResultRest;
import org.jboss.pnc.common.Random;
import org.jboss.pnc.enums.BuildStatus;

import java.time.Instant;
import java.util.Map;

import static java.time.temporal.ChronoUnit.MINUTES;
import static java.util.Date.from;

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
