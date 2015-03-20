package org.jboss.pnc.core.test.buildCoordinator.event;

import org.jboss.pnc.spi.events.BuildStatusChangedEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

@ApplicationScoped
public class TestCDIBuildStatusChangedReceiver {

    public static final TestCDIBuildStatusChangedReceiver INSTANCE = new TestCDIBuildStatusChangedReceiver();

    private List<Consumer<BuildStatusChangedEvent>> listeners = new LinkedList<>();

    public void addBuildStatusChangedEventListener(Consumer<BuildStatusChangedEvent> listener) {
        listeners.add(listener);
    }

    public void collectEvent(@Observes BuildStatusChangedEvent buildStatusChangedEvent) {
        listeners.stream().forEach(listener -> listener.accept(buildStatusChangedEvent));
    }

    public void clear() {
        listeners.clear();
    }
}
