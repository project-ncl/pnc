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
package org.jboss.pnc.spi.builddriver;

import java.util.function.Consumer;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * Date: 7/22/16
 * Time: 11:10 AM
 */
public class DebugData {
    private String sshHost;
    private Integer sshPort;
    private String sshPassword;
    private boolean debugEnabled = false;
    private Consumer<DebugData> sshServiceInitializer = d -> {
        throw new IllegalStateException("No initializer for ssh service provided");
    };
    private final boolean enableDebugOnFailure;

    public DebugData(boolean enableDebugOnFailure) {
        this.enableDebugOnFailure = enableDebugOnFailure;
    }

    public String getSshHost() {
        return sshHost;
    }

    public void setSshHost(String sshHost) {
        this.sshHost = sshHost;
    }

    public Integer getSshPort() {
        return sshPort;
    }

    public void setSshPort(Integer sshPort) {
        this.sshPort = sshPort;
    }

    public String getSshPassword() {
        return sshPassword;
    }

    public void setSshPassword(String password) {
        this.sshPassword = password;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public void setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }

    public boolean isEnableDebugOnFailure() {
        return enableDebugOnFailure;
    }

    public void setSshServiceInitializer(Consumer<DebugData> sshServiceInitializer) {
        this.sshServiceInitializer = sshServiceInitializer;
    }

    public Consumer<DebugData> getSshServiceInitializer() {
        return sshServiceInitializer;
    }
}
