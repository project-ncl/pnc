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
package org.jboss.pnc.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Edge in graph of objects.
 *
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@ToString
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = Edge.Builder.class)
public class Edge<T> {

    /**
     * Source vertex name.
     */
    private final String source;

    /**
     * Target vertex name.
     */
    private final String target;

    /**
     * Edge cost.
     */
    private final int cost;

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder<T> {
    }
}
