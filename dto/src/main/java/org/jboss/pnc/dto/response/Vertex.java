/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
import lombok.Getter;

/**
 * Vertex in graph of objects.
 *
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Getter
@AllArgsConstructor
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = Vertex.Builder.class)
public class Vertex<T> {

    /**
     * Name of the object.
     */
    private final String name;

    /**
     * The object type.
     */
    private final String dataType;

    /**
     * The object stored in the vertex.
     */
    private final T data;

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder<T> {
    }
}
