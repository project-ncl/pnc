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
import lombok.Builder;
import lombok.Data;
import org.jboss.pnc.enums.ValidationErrorType;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Response to a validation request.
 *
 * @author <a href="mailto:jmichalo@redhat.com">Jan Michalov</a>
 */
@Data
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = ValidationResponse.Builder.class)
public class ValidationResponse {

    /**
     * Is the data in the request valid?
     */
    @NotNull
    public final Boolean isValid;

    /**
     * If the data in the request are not valid, the validation error type. If they are valid this is null.
     * 
     * @see ValidationErrorType
     */
    public final ValidationErrorType errorType;

    /**
     * User readable validation hints.
     */
    public List<String> hints;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }
}
