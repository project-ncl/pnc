/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.termdbuilddriver.websockets;

import org.jboss.pnc.termdbuilddriver.AbstractLocalBuildAgentTest;
import org.jboss.pnc.termdbuilddriver.commands.InvocatedCommandResult;
import org.jboss.pnc.termdbuilddriver.commands.TermdCommandInvoker;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TermdCommandInvokerTest extends AbstractLocalBuildAgentTest {

    @Test(timeout = 60_000)
    public void shouldInvokeRemoteCommand() throws Exception {
        //given
        TermdCommandInvoker termdCommandInvoker = new TermdCommandInvoker(baseBuildAgentUri, localEnvironmentPointer.getWorkingDirectory());

        //when
        termdCommandInvoker.startSession();
        InvocatedCommandResult invocationData = termdCommandInvoker.performCommand("echo test").get();
        termdCommandInvoker.closeSession();

        //then
        assertThat(invocationData.getTaskId()).isNotEqualTo(-1);
        assertThat(invocationData.isSucceed()).isEqualTo(true);
    }

}
