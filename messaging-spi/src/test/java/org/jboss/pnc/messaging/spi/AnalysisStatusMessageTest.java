/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * @author jakubvanko
 */
package org.jboss.pnc.messaging.spi;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

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
