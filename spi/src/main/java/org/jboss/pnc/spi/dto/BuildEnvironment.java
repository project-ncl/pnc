/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.spi.dto;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.jboss.pnc.model.SystemImageType;

import java.util.Map;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Deprecated
@Data
@AllArgsConstructor
@Builder(builderClassName = "Builder")
public class BuildEnvironment implements DTOEntity {

    private final Integer id;

    private final String name;

    private final String description;

    private final String systemImageRepositoryUrl;

    private final String systemImageId;

    private final Map<String, String> attributes;

    private final SystemImageType systemImageType;

    private final boolean deprecated;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }
}
