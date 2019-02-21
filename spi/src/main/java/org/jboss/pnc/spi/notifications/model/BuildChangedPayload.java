/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;
import org.jboss.pnc.dto.Build;

@Data
@Builder(buildMethodName = "buildMe")
@JsonDeserialize(builder = BuildChangedPayload.BuildChangedPayloadBuilder.class)
public class BuildChangedPayload implements NotificationPayload {

    private final String oldStatus;

    private final Build build;

    @JsonIgnore
    @Override
    public Integer getId() {
        return build.getId();
    }

    @JsonIgnore
    @Override
    public Integer getUserId() {
        return build.getUser().getId();
    }

    @JsonPOJOBuilder(withPrefix = "", buildMethodName = "buildMe")
    public static final class BuildChangedPayloadBuilder {
    }

}

