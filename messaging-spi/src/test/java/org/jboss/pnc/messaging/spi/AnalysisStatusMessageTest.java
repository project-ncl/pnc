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

/**
 * @author jakubvanko
 */
package org.jboss.pnc.messaging.spi;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author jakubvanko
 */
@RunWith(MockitoJUnitRunner.class)
public class AnalysisStatusMessageTest {

    @Test
    public void analysisStatusMessageShouldReturnCorrectJSON() {
        List<String> deliverablesUrls = new ArrayList<>();
        deliverablesUrls.add("test-link1");
        deliverablesUrls.add("test-link2");

        Message message = new AnalysisStatusMessage(
                "test-operation-id",
                "test-attribute",
                "test-milestone-id",
                "test-status",
                "test-result",
                deliverablesUrls);

        assertThat(message.toJson()).isEqualTo(
                "{\"operationId\":\"test-operation-id\"," + "\"attribute\":\"test-attribute\","
                        + "\"milestoneId\":\"test-milestone-id\"," + "\"status\":\"test-status\","
                        + "\"result\":\"test-result\"" + ",\"deliverablesUrls" + "\":[\"test-link1\",\"test-link2\"]}");

    }

}
