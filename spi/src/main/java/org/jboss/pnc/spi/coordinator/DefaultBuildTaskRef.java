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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.enums.BuildCoordinationStatus;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.User;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@Builder(toBuilder = true)
@ToString
public class DefaultBuildTaskRef implements BuildTaskRef {

    @Getter
    private final String id;

    @Getter
    private final IdRev idRev;

    @Getter
    private final BuildConfigSetRecord buildConfigSetRecord;

    @Getter
    private final ProductMilestone productMilestone;

    @Getter
    private final String contentId;

    @Getter
    private final Instant submitTime;

    @Getter
    private final Instant startTime;

    @Getter
    private final Instant endTime;

    @Getter
    private final User user;

    @Getter
    private final BuildCoordinationStatus status;

    @Getter
    private final boolean temporaryBuild;

    @Getter
    private final AlignmentPreference alignmentPreference;

    @Getter
    private final BuildRecord noRebuildCause;

    @Getter
    @Builder.Default
    private final Set<String> dependants = new HashSet<>();

    @Getter
    @Builder.Default
    private final Set<String> dependencies = new HashSet<>();

    @Getter
    @Builder.Default
    private final Set<String> taskDependants = new HashSet<>();

    @Getter
    @Builder.Default
    private final Set<String> taskDependencies = new HashSet<>();

}
