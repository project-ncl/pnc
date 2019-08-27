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
package org.jboss.pnc.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;

import org.jboss.pnc.constants.Patterns;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;
import org.jboss.pnc.processor.annotation.PatchSupport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;
import java.util.Map;

import static org.jboss.pnc.processor.annotation.PatchSupport.Operation.ADD;
import static org.jboss.pnc.processor.annotation.PatchSupport.Operation.REMOVE;
import static org.jboss.pnc.processor.annotation.PatchSupport.Operation.REPLACE;

/**
 *
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 */
@Data
@Builder(builderClassName = "Builder", builderMethodName = "refBuilder")
@JsonDeserialize(builder = ProductVersionRef.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductVersionRef implements DTOEntity {
    @NotNull(groups = WhenUpdating.class)
    @Null(groups = WhenCreatingNew.class)
    protected final Integer id;

    @PatchSupport({REPLACE})
    @NotNull(groups = {WhenCreatingNew.class, WhenUpdating.class})
    @Pattern(message="The version should consist of two numeric parts separated by a dot" , regexp=Patterns.PRODUCT_STREAM_VERSION, groups = {WhenCreatingNew.class, WhenUpdating.class})
    protected final String version;

    @PatchSupport({ADD, REMOVE, REPLACE})
    protected final Map<String, String> attributes;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
