/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

package org.jboss.pnc.facade.deliverables;

import org.jboss.pnc.enums.AnalysisStatus;

import java.util.List;

/**
 * @author jakubvanko
 */
public class DefaultAnalysisStatusChangedEvent implements AnalysisStatusChangedEvent {

    private final AnalysisStatus status;
    private final String milestoneId;
    private final List<String> sourcesLinks;

    public DefaultAnalysisStatusChangedEvent(AnalysisStatus status, String milestoneId, List<String> sourcesLinks) {
        this.status = status;
        this.milestoneId = milestoneId;
        this.sourcesLinks = sourcesLinks;
    }

    @Override
    public AnalysisStatus getStatus() {
        return status;
    }

    @Override
    public String getMilestoneId() {
        return milestoneId;
    }

    @Override
    public List<String> getSourcesLinks() {
        return sourcesLinks;
    }

    @Override
    public String toString() {
        return "DefaultAnalysisStatusChangedEvent{" + "status=" + status + ", milestoneId=" + milestoneId
                + ", sourcesLinks=" + String.join(";", sourcesLinks) + '}';
    }
}
