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

package org.jboss.pnc.coordinator.builder.bpm;

import org.jboss.pnc.spi.coordinator.BuildScheduler;
import org.jboss.pnc.spi.coordinator.BuildSetTask;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.exception.CoreException;

import javax.enterprise.context.ApplicationScoped;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@ApplicationScoped
public class BpmBuildScheduler implements BuildScheduler {

    @Deprecated
    public BpmBuildScheduler() { // CDI workaround
    }

    @Override
    public void startBuilding(BuildTask buildTask) throws CoreException {
        throw new UnsupportedOperationException("Will be removed.");
    }

    @Override
    public void startBuilding(BuildSetTask buildSetTask) throws CoreException {
        throw new UnsupportedOperationException("Only to be used with remote build scheduler.");
    }

    @Override
    public boolean cancel(BuildTask buildTask) {
        throw new UnsupportedOperationException("Will be removed.");
    }
}
