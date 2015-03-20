package org.jboss.pnc.core.events;

import org.jboss.pnc.spi.events.BuildStatusChangedEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.function.Consumer;

@ApplicationScoped
public class StatusChangedBroadcastAdapter implements Consumer<BuildStatusChangedEvent> {

    private Event<BuildStatusChangedEvent> eventSender;

    @Deprecated
    public StatusChangedBroadcastAdapter() {
    }

    @Inject
    public StatusChangedBroadcastAdapter(Event<BuildStatusChangedEvent> eventSender) {
        this.eventSender = eventSender;
    }

    @Override
    public void accept(BuildStatusChangedEvent buildStatusChangedEvent) {
        eventSender.fire(buildStatusChangedEvent);
    }
}
