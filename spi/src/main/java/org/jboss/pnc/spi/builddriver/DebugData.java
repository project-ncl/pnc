/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.builddriver;

import java.util.function.Consumer;

import org.jboss.pnc.spi.SshCredentials;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 7/22/16 Time: 11:10 AM
 */
public class DebugData {

    private SshCredentials sshCredentials = new SshCredentials();
    private boolean debugEnabled = false;
    private Consumer<DebugData> sshServiceInitializer = d -> {
        throw new IllegalStateException("No initializer for ssh service provided");
    };
    private final boolean enableDebugOnFailure;

    public DebugData(boolean enableDebugOnFailure) {
        this.enableDebugOnFailure = enableDebugOnFailure;
    }

    public void setSshCommand(String sshHost) {
        this.sshCredentials.setCommand(sshHost);
    }

    public void setSshPassword(String password) {
        this.sshCredentials.setPassword(password);
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

    public SshCredentials getSshCredentials() {
        return sshCredentials;
    }
}
