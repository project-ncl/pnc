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
package org.jboss.pnc.coordinator.test;

import org.jboss.pnc.coordinator.builder.BuildQueue;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 4/26/16 Time: 9:29 AM
 */
public class BuildCoordinatorBeans {
    public final BuildQueue queue;
    public final BuildCoordinator coordinator;

    public BuildCoordinatorBeans(BuildQueue queue, BuildCoordinator coordinator) {
        this.queue = queue;
        this.coordinator = coordinator;
    }
}
