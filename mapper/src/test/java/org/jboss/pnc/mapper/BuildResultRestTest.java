/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mapper;

import org.jboss.pnc.api.enums.orch.CompletionStatus;
import org.jboss.pnc.dto.internal.BuildResultRest;
import org.jboss.pnc.dto.internal.EnvironmentDriverResultRest;
import org.jboss.pnc.dto.internal.RepourResultRest;
import org.junit.Test;

/**
 * @author Jakub Bartecek
 */
public class BuildResultRestTest {

    @Test
    public void shouldGetLimitedToStringWithNulls() {
        BuildResultRest buildResultRest = new BuildResultRest();
        buildResultRest.toString();
    }

    @Test
    public void shouldGetLimitedToStringWithSomeValues() {
        BuildResultRest buildResultRest = new BuildResultRest();

        buildResultRest.setCompletionStatus(CompletionStatus.SUCCESS);
        buildResultRest.setProcessException(null);
        buildResultRest.setBuildExecutionConfiguration(null);
        buildResultRest.setBuildDriverResult(null);
        buildResultRest.setRepositoryManagerResult(null);

        EnvironmentDriverResultRest environmentDriverResult = new EnvironmentDriverResultRest(
                CompletionStatus.SUCCESS,
                null);
        buildResultRest.setEnvironmentDriverResult(environmentDriverResult);

        buildResultRest
                .setRepourResult(new RepourResultRest(CompletionStatus.SUCCESS, "org.jboss", "1.1.0.Final-redhat-1"));

        buildResultRest.toString();
    }

}
