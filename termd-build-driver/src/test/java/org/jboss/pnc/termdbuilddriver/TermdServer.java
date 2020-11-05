/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

package org.jboss.pnc.termdbuilddriver;

import org.jboss.pnc.buildagent.common.RandomUtils;
import org.jboss.pnc.buildagent.server.BuildAgentException;
import org.jboss.pnc.buildagent.server.BuildAgentServer;
import org.jboss.pnc.buildagent.server.IoLoggerName;
import org.jboss.pnc.buildagent.server.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class TermdServer {

    private static final AtomicInteger port_pool = new AtomicInteger(8090);

    private static BuildAgentServer buildAgent;

    private static final Logger log = LoggerFactory.getLogger(TermdServer.class);

    private final static AtomicReference<Integer> runningPort = new AtomicReference<>();

    public static int getNextPort() {
        return port_pool.getAndIncrement();
    }

    /**
     * Try to start the build agent and block until it is up and running.
     *
     * @return
     * @throws InterruptedException
     * @param host
     * @param port
     * @param bindPath
     */
    public static void startServer(String host, int port, String bindPath, Optional<Path> logFolder) {
        try {
            IoLoggerName[] primaryLoggers = { IoLoggerName.FILE };
            Options options = new Options(host, port, bindPath, true, false, 3, 100);
            Map<String, String> mdcMap = new HashMap<>();
            mdcMap.put("ctx", RandomUtils.randString(6));
            buildAgent = new BuildAgentServer(logFolder, Optional.empty(), primaryLoggers, options, mdcMap);
            log.info("Server started.");
        } catch (BuildAgentException e) {
            throw new RuntimeException("Cannot start build agent.", e);
        }
        runningPort.set(buildAgent.getPort());
    }

    public static AtomicInteger getPort_pool() {
        return port_pool;
    }

    public static Integer getPort() {
        return runningPort.get();
    }

    public static void stopServer() {
        log.info("Stopping server...");
        buildAgent.stop();
    }

}
