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

import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.pnc.spi.executor.exceptions.ExecutorException;

/**
 * BuildScheduler is used to direct the build to by scheduler defined execution engine. Example: BuildCoordinator uses
 * BuildScheduler to start the builds and depending on BuildScheduler implementation builds can be pushed to BPM engine
 * (BpmBuildScheduler) or submitted directly (LocalBuildScheduler).
 *
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public interface BuildScheduler {

    // TODO remove after in-memory scheduling gets removed and Rex is stable
    @Deprecated
    void startBuilding(BuildTask buildTask) throws CoreException;

    void startBuilding(BuildSetTask buildSetTask) throws CoreException;

    boolean cancel(BuildTask buildTask) throws CoreException;
}
