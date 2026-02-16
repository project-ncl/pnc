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
package org.jboss.pnc.messaging.spi;

import org.jboss.pnc.api.enums.AttachmentType;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.dto.Attachment;
import org.jboss.pnc.dto.BuildRef;
import org.jboss.pnc.enums.BuildStatus;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

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
                        .md5("160da473687d08ba021dd48e60786baf")
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
