package org.jboss.pnc.jenkinsbuilddriver.buildmonitor;

import java.util.concurrent.ScheduledFuture;
import java.util.function.Consumer;

/**
* Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-12.
*/
class MonitorTask {

    final Consumer<String> onMonitorComplete;
    final Consumer<Exception> onMonitorError;
    private ScheduledFuture cancelHook;

    public MonitorTask(Consumer<String> onMonitorComplete, Consumer<Exception> onMonitorError) {
        this.onMonitorComplete = onMonitorComplete;
        this.onMonitorError = onMonitorError;
    }

    public void setCancelHook(ScheduledFuture cancelHook) {
        this.cancelHook = cancelHook;
    }

    public ScheduledFuture getCancelHook() {
        return cancelHook;
    }
}
