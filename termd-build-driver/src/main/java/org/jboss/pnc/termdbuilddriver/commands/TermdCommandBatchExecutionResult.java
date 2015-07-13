/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.termdbuilddriver.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TermdCommandBatchExecutionResult {

    private List<InvocatedCommandResult> commandResults;
    private boolean isSuccessful;

    public TermdCommandBatchExecutionResult(Collection<InvocatedCommandResult> results) {
        this.commandResults = new ArrayList<>(results);
        this.isSuccessful = this.commandResults.stream()
                .filter(result -> result.isSucceed() == false)
                .count() == 0;
    }

    public List<InvocatedCommandResult> getCommandResults() {
        return commandResults;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }
}
