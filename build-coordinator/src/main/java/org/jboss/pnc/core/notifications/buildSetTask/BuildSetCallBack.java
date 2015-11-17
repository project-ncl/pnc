/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.core.notifications.buildSetTask;

import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;

import java.util.function.Consumer;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildSetCallBack {
    private Integer buildSetTaskId;
    private Consumer<BuildSetStatusChangedEvent> callback;

    @Deprecated //CDI workaround
    public BuildSetCallBack() {
        buildSetTaskId = -1;//CDI arquillian workaround
    }

    public BuildSetCallBack(int buildSetTaskId, Consumer<BuildSetStatusChangedEvent> callback) {
        this.buildSetTaskId = buildSetTaskId;
        this.callback = callback;
    }

    public Integer getBuildSetTaskId() {
        return buildSetTaskId;
    }

    public void callback(BuildSetStatusChangedEvent buildSetStatusChangedEvent) {
        if (callback != null) {
            callback.accept(buildSetStatusChangedEvent);
        }
    }
}
