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
package org.jboss.pnc.spi.coordinator;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.model.IdRev;

import java.util.Date;
import java.util.List;

@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildMeta {
    @Getter
    String id;

    @Getter
    IdRev idRev;

    @Getter
    String contentId;

    @Getter
    boolean temporaryBuild;

    @Getter
    AlignmentPreference alignmentPreference;

    @Getter
    Date submitTime;

    @Getter
    String username;

    @Getter
    Integer productMilestoneId;

    @Getter
    String noRebuildCauseId;

    @Getter
    List<String> dependants;

    @Getter
    List<String> dependencies;
}
