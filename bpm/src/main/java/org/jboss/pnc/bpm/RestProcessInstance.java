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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * { "process-instance-id": 1, "process-id": "Employee_Rostering.Process1", "process-name": "Process1",
 * "process-version": "1.0", "process-instance-state": 1, "container-id": "employee-rostering", "initiator": "baAdmin",
 * "start-date": { "java.util.Date": 1539184095041 }, "process-instance-desc": "Process1", "correlation-key": "1",
 * "parent-instance-id": -1, "sla-compliance": 0, "sla-due-date": null, "active-user-tasks": null,
 * "process-instance-variables": null }
 * 
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */

public class RestProcessInstance {

    @JsonProperty("process-instance-id")
    private long id;

    @JsonProperty("container-id")
    private String containerId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

}
