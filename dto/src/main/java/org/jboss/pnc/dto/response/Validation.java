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

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * A request validation error.
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@Builder(builderClassName = "Builder")
@AllArgsConstructor()
@JsonDeserialize(builder = Validation.Builder.class)
public class Validation {

    /**
     * Identifier of the attribute which didn't pass validation.
     */
    private final String attribute;

    /**
     * User readable validation messages.
     */
    private final List<String> messages;

    /**
     * The original non-valid value. Based on the validation this may not be set.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Object value;

    public Validation(String attribute, String message, Object value) {
        this.attribute = attribute;
        this.messages = Collections.singletonList(message);
        this.value = value;
    }

    public Validation(String attribute, String message) {
        this.attribute = attribute;
        this.messages = Collections.singletonList(message);
        this.value = null;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }
}
