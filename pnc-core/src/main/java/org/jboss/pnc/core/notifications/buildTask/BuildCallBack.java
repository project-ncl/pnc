package org.jboss.pnc.core.notifications.buildTask;

import org.jboss.pnc.spi.events.BuildStatusChangedEvent;

import java.util.function.Consumer;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildCallBack {

    private Integer buildTaskId;
    private Consumer<BuildStatusChangedEvent> callback;

    public BuildCallBack(int buildTaskId, Consumer<BuildStatusChangedEvent> callback) {
        this.buildTaskId = buildTaskId;
        this.callback = callback;
    }

    public Integer getBuildTaskId() {
        return buildTaskId;
    }

    public void callback(BuildStatusChangedEvent buildStatusChangedEvent) {
        callback.accept(buildStatusChangedEvent);
    }

}
