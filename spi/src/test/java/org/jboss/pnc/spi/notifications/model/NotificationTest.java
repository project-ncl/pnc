/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.spi.notifications.model;

import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfigurationRevisionRef;
import org.jboss.pnc.dto.BuildEnvironment;
import org.jboss.pnc.dto.ProjectRef;
import org.jboss.pnc.dto.SCMRepository;
import org.jboss.pnc.dto.User;
import org.jboss.pnc.enums.BuildCoordinationStatus;
import org.jboss.pnc.enums.SystemImageType;
import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.util.Arrays;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class NotificationTest {

    @Test
    public void shouldDeserialize() throws Exception {
        ProjectRef projectRef = ProjectRef.refBuilder()
            .id(1)
            .name("A")
            .description("desc")
            .projectUrl("url1")
            .issueTrackerUrl("url2")
            .build();

        SCMRepository scmRepository = SCMRepository.builder()
                .id(1)
                .internalUrl("url1")
                .externalUrl("url2")
                .preBuildSyncEnabled(true)
                .build();

        BuildEnvironment buildEnvironment = BuildEnvironment.builder()
                .id(1)
                .name("jdk8")
                .description("desc")
                .systemImageRepositoryUrl("url")
                .systemImageId("11")
                .systemImageType(SystemImageType.DOCKER_IMAGE)
                .deprecated(true)
                .build();

        User user = User.builder()
                .id(1)
                .username("user")
                .build();
        BuildConfigurationRevisionRef buildConfigurationRevisionRef = BuildConfigurationRevisionRef.refBuilder()
                .id(1)
                .rev(1)
                .name("name")
                .description("desc")
                .buildScript("true")
                .scmRevision("awqs21")
                .build();

        Build build = Build.builder()
                .id(1)
                .submitTime(Instant.ofEpochMilli(1526473388394L))
                .project(projectRef)
                .repository(scmRepository)
                .environment(buildEnvironment)
                .user(user)
                .buildConfigurationAudited(buildConfigurationRevisionRef)
                .status(BuildCoordinationStatus.BUILDING)
                .buildContentId("build-42")
                .temporaryBuild(true)
                .dependencyBuildIds(Arrays.asList(new Integer[]{1,2,3}))
                .build();

        BuildChangedPayload buildChangedPayload = BuildChangedPayload.builder()
                .oldStatus(BuildCoordinationStatus.BUILDING.toString())
                .build(build)
                .buildMe();
        String buildChangedPayloadString = JsonOutputConverterMapper.apply(buildChangedPayload);
        System.out.println(buildChangedPayloadString);

        String serialized = "{\"eventType\":\"BUILD_STATUS_CHANGED\",\"payload\":{\"oldStatus\":\"BUILDING\",\"build\":{\"id\":1,\"submitTime\":1526473388.394000000,\"status\":\"BUILDING\",\"buildContentId\":\"build-42\",\"temporaryBuild\":true,\"project\":{\"id\":1,\"name\":\"A\",\"description\":\"desc\",\"issueTrackerUrl\":\"url2\",\"projectUrl\":\"url1\"},\"repository\":{\"id\":1,\"internalUrl\":\"url1\",\"externalUrl\":\"url2\",\"preBuildSyncEnabled\":true},\"environment\":{\"id\":1,\"name\":\"jdk8\",\"description\":\"desc\",\"systemImageRepositoryUrl\":\"url\",\"systemImageId\":\"11\",\"systemImageType\":\"DOCKER_IMAGE\",\"deprecated\":true},\"user\":{\"id\":1,\"username\":\"user\"},\"buildConfigurationAudited\":{\"id\":1,\"rev\":1,\"name\":\"name\",\"description\":\"desc\",\"buildScript\":\"true\",\"scmRevision\":\"awqs21\"},\"dependencyBuildIds\":[1,2,3]}}}";
        Notification notification = new Notification(serialized);

        Assert.assertEquals(EventType.BUILD_STATUS_CHANGED, notification.getEventType());

    }
}
