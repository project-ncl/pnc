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

import org.assertj.core.api.Assertions;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfigurationRevisionRef;
import org.jboss.pnc.dto.Environment;
import org.jboss.pnc.dto.ProjectRef;
import org.jboss.pnc.dto.SCMRepository;
import org.jboss.pnc.dto.User;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.enums.SystemImageType;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildStatusChangedTest {

    Logger logger = LoggerFactory.getLogger(BuildStatusChangedTest.class);

    @Test
    public void shouldSerializeObject() throws IOException {
        // given

        Build build = getBuild();

        BuildStatus oldStatus = BuildStatus.NEW;

        BuildStatusChanged buildStatusChanged = BuildStatusChanged.builder()
                .oldStatus(oldStatus.toString())
                .build(build)
                .buildMe();

        // when
        String serialized = buildStatusChanged.toJson();
        logger.info("Serialized: {}", serialized);

        BuildStatusChanged deserialized = JsonOutputConverterMapper.readValue(serialized, BuildStatusChanged.class);

        // then
        Assertions.assertThat(deserialized).isEqualToComparingFieldByField(buildStatusChanged);

    }

    private Build getBuild() {
        ProjectRef projectRef = ProjectRef.refBuilder()
                .id("1")
                .name("A")
                .description("desc")
                .projectUrl("url1")
                .issueTrackerUrl("url2")
                .build();

        SCMRepository scmRepository = SCMRepository.builder()
                .id("1")
                .internalUrl("url1")
                .externalUrl("url2")
                .preBuildSyncEnabled(true)
                .build();

        Environment buildEnvironment = Environment.builder()
                .id("1")
                .name("jdk8")
                .description("desc")
                .systemImageRepositoryUrl("url")
                .systemImageId("11")
                .systemImageType(SystemImageType.DOCKER_IMAGE)
                .deprecated(true)
                .build();

        User user = User.builder().id("1").username("user").build();
        BuildConfigurationRevisionRef buildConfigurationRevisionRef = BuildConfigurationRevisionRef.refBuilder()
                .id("1")
                .rev(1)
                .name("name")
                .buildScript("true")
                .scmRevision("awqs21")
                .build();

        return Build.builder()
                .project(projectRef)
                .scmRepository(scmRepository)
                .environment(buildEnvironment)
                .user(user)
                .buildConfigRevision(buildConfigurationRevisionRef)
                .status(BuildStatus.BUILDING)
                .buildContentId("build-42")
                .temporaryBuild(true)
                .build();
    }
}
