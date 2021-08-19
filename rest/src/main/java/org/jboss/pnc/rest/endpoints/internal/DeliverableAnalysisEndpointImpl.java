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
package org.jboss.pnc.rest.endpoints.internal;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.pnc.api.deliverablesanalyzer.dto.AnalysisResult;
import org.jboss.pnc.api.deliverablesanalyzer.dto.FinderResult;
import org.jboss.pnc.enums.AnalysisStatus;
import org.jboss.pnc.facade.deliverables.DefaultAnalysisStatusChangedEvent;
import org.jboss.pnc.facade.deliverables.DeliverableAnalyzerResultProcessor;
import org.jboss.pnc.facade.util.UserService;
import org.jboss.pnc.mapper.api.ProductMilestoneMapper;
import org.jboss.pnc.model.User;
import org.jboss.pnc.rest.endpoints.internal.api.DeliverableAnalysisEndpoint;
import org.jboss.pnc.facade.deliverables.AnalysisStatusChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class DeliverableAnalysisEndpointImpl implements DeliverableAnalysisEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(DeliverableAnalysisEndpointImpl.class);

    @Inject
    private DeliverableAnalyzerResultProcessor resultProcessor;

    @Inject
    private UserService userService;

    @Inject
    private ProductMilestoneMapper milestoneMapper;

    @Inject
    private Event<AnalysisStatusChangedEvent> analysisStatusChangedEventNotifier;

    @Override
    public void completeAnalysis(AnalysisResult result) {
        User user = userService.currentUser();

        int milestoneId = milestoneMapper.getIdMapper().toEntity(result.getMilestoneId());
        List<String> sourcesLinks = new ArrayList<>();

        for (FinderResult finderResult : result.getResults()) {
            resultProcessor
                    .processDeliverables(milestoneId, finderResult.getBuilds(), finderResult.getUrl().toString(), user);
            sourcesLinks.add(finderResult.getUrl().toString());
        }

        AnalysisStatusChangedEvent analysisStatusChanged = new DefaultAnalysisStatusChangedEvent(
                AnalysisStatus.COMPLETED,
                result.getMilestoneId(),
                sourcesLinks);
        analysisStatusChangedEventNotifier.fire(analysisStatusChanged);
    }
}
