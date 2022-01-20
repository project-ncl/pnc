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
package org.jboss.pnc.dto.requests.validation;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * Request for explicit product milestone version validation.
 *
 * @author jmichalo <jmichalo@redhat.com>
 */
@Data
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = VersionValidationRequest.Builder.class)
public class VersionValidationRequest {

    /**
     * Id of the product version. The product version is used to prevent duplicate milestones with the same milestone
     * version.
     */
    @NotBlank
    public final String productVersionId;

    /**
     * The version to be validated.
     */
    @NotBlank
    public final String version;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }
}
