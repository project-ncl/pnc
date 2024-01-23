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
package org.jboss.pnc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotNull;

/**
 * DTO for information about the actual status of the PNC: announcement banner, ETA and maintenance mode.
 */
@Value
@Builder(builderClassName = "Builder", toBuilder = true)
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode
@ToString
public class PncStatus {

    /**
     * Announcement banner. Required for every type of request.
     */
    @NotNull
    String banner;

    /**
     * ETA of maintenance mode being ended.
     */
    String eta;

    /**
     * Is the maintenance mode active?
     * <p/>
     * Note: When we're setting only the banner, do not specify maintenance mode. Hence, it's nullable (=Boolean).
     */
    Boolean isMaintenanceMode;
}
