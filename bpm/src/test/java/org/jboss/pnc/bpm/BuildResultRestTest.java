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
package org.jboss.pnc.bpm;

import org.jboss.pnc.bpm.model.BuildResultRest;
import org.jboss.pnc.spi.coordinator.CompletionStatus;
import org.jboss.pnc.spi.environment.EnvironmentDriverResult;
import org.jboss.pnc.spi.repour.RepourResult;
import org.junit.Test;

import java.util.Optional;

/**
 * @author Jakub Bartecek
 */
public class BuildResultRestTest {

    private final String LOG = "LOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO"
            + "GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG";

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
        buildResultRest.setProcessLog(LOG);
        buildResultRest.setBuildExecutionConfiguration(null);
        buildResultRest.setBuildDriverResult(null);
        buildResultRest.setRepositoryManagerResult(null);

        EnvironmentDriverResult environmentDriverResult = new EnvironmentDriverResult(
                CompletionStatus.SUCCESS,
                "SUCCESS",
                Optional.empty());
        buildResultRest.setEnvironmentDriverResult(environmentDriverResult);

        buildResultRest
                .setRepourResult(new RepourResult(CompletionStatus.SUCCESS, "org.jboss", "1.1.0.Final-redhat-1"));

        buildResultRest.toString();
    }

}
