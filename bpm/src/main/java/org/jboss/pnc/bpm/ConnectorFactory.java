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
package org.jboss.pnc.bpm;

import org.jboss.pnc.common.json.moduleconfig.BpmModuleConfig;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@ApplicationScoped
public class ConnectorFactory {

    private BpmModuleConfig bpmConfig;

    private Map<String, Connector> connectors = new ConcurrentHashMap<>();
    private boolean useMock;

    @Deprecated // required by CDI
    public ConnectorFactory() {
    }

    @Inject
    public ConnectorFactory(BpmModuleConfig bpmConfig) {
        this.bpmConfig = bpmConfig;
    }

    public Connector get() {
        if (useMock) {
            return connectors.computeIfAbsent("mock", (k) -> new MockConnector());
        } else {
            return connectors.computeIfAbsent("rest", (k) -> new RestConnector(bpmConfig));
        }
    }

    public void setUseMock(boolean useMock) {
        this.useMock = useMock;
    }
}
