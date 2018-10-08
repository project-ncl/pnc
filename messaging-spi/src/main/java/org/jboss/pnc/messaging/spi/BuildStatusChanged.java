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
package org.jboss.pnc.messaging.spi;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Builder()
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
@Getter
@JsonDeserialize(builder = BuildStatusChanged.BuildStatusChangedBuilder.class)
public class BuildStatusChanged implements Message {

    private final String attribute = "state";

    private final String oldStatus;

    private final String newStatus;

    private final String buildRecordId;

    @Override
    public String toJson() {
        return JsonOutputConverterMapper.apply(this);
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class BuildStatusChangedBuilder {
    }
}
