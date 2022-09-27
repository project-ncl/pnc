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
package org.jboss.pnc.messaging;

import org.jboss.pnc.common.json.moduleconfig.SystemConfig;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Dependent
public class SysConfigProducer {

    @Produces
    public SystemConfig createSystemConfig() {
        // SystemConfig systemConfig = Mockito.mock(SystemConfig.class);
        // Mockito.when(systemConfig.getMessagingInternalQueueSize()).thenReturn(2);
        // return systemConfig;

        return new SystemConfig(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "10",
                null,
                null,
                null,
                "",
                "2",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }
}
