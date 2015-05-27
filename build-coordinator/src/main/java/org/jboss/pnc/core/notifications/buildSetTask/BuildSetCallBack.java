package org.jboss.pnc.core.notifications.buildSetTask;

import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;

import java.util.function.Consumer;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildSetCallBack {
    private Integer buildSetTaskId;
    private Consumer<BuildSetStatusChangedEvent> callback;

    public BuildSetCallBack(int buildSetTaskId, Consumer<BuildSetStatusChangedEvent> callback) {
        this.buildSetTaskId = buildSetTaskId;
        this.callback = callback;
    }

    public Integer getBuildSetTaskId() {
        return buildSetTaskId;
    }

    public void callback(BuildSetStatusChangedEvent buildSetStatusChangedEvent) {
        callback.accept(buildSetStatusChangedEvent);
    }
}
