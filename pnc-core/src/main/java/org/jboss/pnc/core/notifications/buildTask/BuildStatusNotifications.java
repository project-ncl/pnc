package org.jboss.pnc.core.notifications.buildTask;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */

import org.jboss.pnc.spi.events.BuildStatusChangedEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@ApplicationScoped
public class BuildStatusNotifications {
    private Set<BuildCallBack> subscribers = new HashSet<>();

    /**
     * Subscriber is automatically removed once task reaches completed state.
     */
    public void subscribe(BuildCallBack buildCallBack) {
        subscribers.add(buildCallBack);
    }

    public void observeEvent(@Observes BuildStatusChangedEvent buildStatusChangedEvent) {
        Predicate<BuildCallBack> filterSubscribersMatchingTaskId =
                (callBackUrl) -> callBackUrl.getBuildTaskId().equals(buildStatusChangedEvent.getBuildTaskId());

        Set<BuildCallBack> matchingTasks = subscribers.stream().filter(filterSubscribersMatchingTaskId).collect(Collectors.toSet());

        matchingTasks.forEach((buildCallBack) -> {
            removeListenersOfCompletedTasks(buildCallBack, buildStatusChangedEvent);
        });
        matchingTasks.forEach((buildCallBack) -> {
            buildCallBack.callback(buildStatusChangedEvent);
        });
    }

    private void removeListenersOfCompletedTasks(BuildCallBack buildCallBack, BuildStatusChangedEvent buildStatusChangedEvent) {
        if (buildStatusChangedEvent.getNewStatus().isCompleted()) {
            subscribers.remove(buildCallBack);
        }
    }
}
