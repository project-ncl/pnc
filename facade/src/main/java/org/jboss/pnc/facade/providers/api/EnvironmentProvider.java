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
package org.jboss.pnc.facade.providers.api;

import org.jboss.pnc.dto.Environment;

public interface EnvironmentProvider
        extends Provider<Integer, org.jboss.pnc.model.BuildEnvironment, Environment, Environment> {

    /**
     * Marks the environment as deprecated and adds attribute to indicate to what environment should be the deprecated
     * upgraded to.
     *
     * @param id ID of the environment to be deprecated;
     * @param replacementId ID of the environment that is replacing the environment.
     * @return The deprecated environment.
     */
    Environment deprecateEnvironment(String id, String replacementId);
}
