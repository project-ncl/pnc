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

import org.jboss.pnc.termdbuilddriver.statusupdates.event.Status;
import org.jboss.pnc.termdbuilddriver.statusupdates.event.UpdateEvent;

import java.net.URI;

public class InvocatedCommandResult {

    private final boolean succeed;
    private final int taskId;
    private final URI baseServerUri;
    private String logsDirectory;

    public InvocatedCommandResult(UpdateEvent event, URI baseServerUri, String logsDirectory) {
        this.baseServerUri = baseServerUri;
        this.succeed = event.getEvent().getNewStatus() == Status.COMPLETED;
        this.taskId = event.getEvent().getTaskId();
        this.logsDirectory = logsDirectory;
    }

    public boolean isSucceed() {
        return succeed;
    }

    public int getTaskId() {
        return taskId;
    }

    public URI getLogsUri() {
        return baseServerUri.resolve("/servlet/download" + logsDirectory + "/console-" + taskId + ".log");
    }

    @Override
    public String toString() {
        return "InvocatedCommandResult{" +
                "succeed=" + succeed +
                ", taskId=" + taskId +
                ", baseServerUri=" + baseServerUri +
                ", logsDirectory='" + logsDirectory + '\'' +
                '}';
    }
}
