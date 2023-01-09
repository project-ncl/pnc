/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.spi.coordinator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jboss.pnc.model.IdRev;

import java.time.Instant;

@AllArgsConstructor
public class DefaultBuildTaskRef implements BuildTaskRef {

    @Getter
    private final String id;

    @Getter
    private final IdRev idRev;

    @Getter
    private final String contentId;

    @Getter
    private final String username;

    @Getter
    private final Instant submitTime;

}
