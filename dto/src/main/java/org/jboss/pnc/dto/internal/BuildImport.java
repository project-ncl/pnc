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
package org.jboss.pnc.dto.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.jboss.pnc.dto.validation.groups.WhenImporting;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.PastOrPresent;
import java.util.Date;

@Getter
@Jacksonized
@Builder(builderClassName = "Builder", toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildImport {

    @NotNull(groups = WhenImporting.class)
    private final @Valid BuildMeta metadata;

    @NotNull(groups = WhenImporting.class)
    private final @Valid BuildResultRest result;

    @NotNull(groups = WhenImporting.class)
    @Past(groups = WhenImporting.class)
    private final Date startTime;

    @NotNull(groups = WhenImporting.class)
    @PastOrPresent(groups = WhenImporting.class)
    private final Date endTime;
}
