/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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

package org.jboss.pnc.mock.model.builders;

import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.User;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class TestEntitiesFactory {
    public static User newUser() {
        return User.Builder.newBuilder()
                .id(1)
                .username("medusa")
                .firstName("Medusa")
                .lastName("Poseidon's")
                .build();
    }

    public static BuildConfigurationSet newBuildConfigurationSet() {
        return BuildConfigurationSet.Builder.newBuilder()
                .id(1)
                .name("test-build-configuration-set-1")
                .build();
    }

}
