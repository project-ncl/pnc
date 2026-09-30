/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.messaging.spi;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.jboss.pnc.api.enums.AttachmentType;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.dto.Attachment;
import org.jboss.pnc.dto.BuildRef;
import org.jboss.pnc.enums.BuildStatus;
import org.junit.Test;

public class BuildAttachmentAddedTest {
    @Test
    public void buildAttachmentAddedShouldReturnCorrectJSON() throws IOException {
        BuildAttachmentAdded message = new BuildAttachmentAdded(
                Attachment.builder()
                        .id("10")
                        .name("Build Log")
                        .url("https://example.com/log.txt")
                        .type(AttachmentType.LOG)
                        .description("description")
                        .creationTime(LocalDateTime.of(2000, 10, 10, 10, 10).toInstant(ZoneOffset.UTC))
                        .sha256("d3d2ca70485f79525be2eff36aada6a397a0ac4072a7e84cb156090bb51c0dc6")
                        .build(getBuild())
                        .build());

        String serialized = message.toJson();
        BuildAttachmentAdded deserialized = JsonOutputConverterMapper.readValue(serialized, BuildAttachmentAdded.class);

        // then
        assertThat(deserialized).usingRecursiveComparison().isEqualTo(message);
    }

    private BuildRef getBuild() {

        return BuildRef.refBuilder()
                .id("BACSKLJ123K")
                .status(BuildStatus.BUILDING)
                .buildContentId("build-42")
                .temporaryBuild(true)
                .build();
    }

}
