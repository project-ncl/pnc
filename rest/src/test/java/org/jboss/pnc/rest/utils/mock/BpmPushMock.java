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
package org.jboss.pnc.rest.utils.mock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.pnc.bpm.BpmTask;
import org.jboss.pnc.bpm.mock.BpmMock;
import org.jboss.pnc.bpm.task.MilestoneReleaseTask;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.spi.exception.CoreException;

import javax.ws.rs.core.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BpmPushMock extends BpmMock {

    private final List<Push> pushes;

    public BpmPushMock() throws ConfigurationParseException, CoreException, IOException {
        pushes = new ArrayList<>();
    }

    @Override
    public boolean startTask(BpmTask task) throws CoreException {
        MilestoneReleaseTask releaseTask = (MilestoneReleaseTask) task;
        String callbackId = RandomStringUtils.randomNumeric(12);
        boolean started = super.startTask(task);
        pushes.add(new Push(releaseTask.getMilestone().getId(), task.getTaskId(), callbackId));
        return started;
    }

    public Response getPushesFor(int milestoneId) {
        List<Push> pushes = this.pushes.stream().filter(p -> p.milestoneId == milestoneId).collect(Collectors.toList());
        return Response.ok(new PushList(pushes)).build();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PushList {
        List<Push> pushes;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Push {
        private int milestoneId;
        private int taskId;
        private String callbackId;
    }

}
