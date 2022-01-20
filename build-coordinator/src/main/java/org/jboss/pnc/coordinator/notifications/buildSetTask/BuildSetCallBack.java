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
package org.jboss.pnc.coordinator.notifications.buildSetTask;

import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;

import java.util.function.Consumer;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildSetCallBack {
    private final Integer buildSetConfigurationId;
    private final Consumer<BuildSetStatusChangedEvent> callback;

    public BuildSetCallBack(int buildSetConfigurationId, Consumer<BuildSetStatusChangedEvent> callback) {
        this.buildSetConfigurationId = buildSetConfigurationId;
        this.callback = callback;
    }

    public Integer getBuildSetConfigurationId() {
        return buildSetConfigurationId;
    }

    public void callback(BuildSetStatusChangedEvent buildSetStatusChangedEvent) {
        callback.accept(buildSetStatusChangedEvent);
    }
}
