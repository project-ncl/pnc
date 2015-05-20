package org.jboss.pnc.core.notifications.buildSetTask;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */

import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@ApplicationScoped
public class BuildSetStatusNotifications {
    private Set<BuildSetCallBack> subscribers = new HashSet<>();

    /**
     * Subscriber is automatically removed once task reaches completed state.
     *
     * @param buildSetCallBack
     */
    public void subscribe(BuildSetCallBack buildSetCallBack) {
        subscribers.add(buildSetCallBack);
    }

    public void observeEvent(@Observes BuildSetStatusChangedEvent buildSetStatusChangedEvent) {
        Predicate<BuildSetCallBack> filterSubscribersMatchingTaskId =
                (callBackUrl) -> callBackUrl.getBuildSetTaskId().equals(buildSetStatusChangedEvent.getBuildSetTaskId());

        Set<BuildSetCallBack> matchingTask = subscribers.stream().filter(filterSubscribersMatchingTaskId).collect(Collectors.toSet());

        matchingTask.forEach((buildSetCallBack) -> {
            removeListenersOfCompletedTasks(buildSetCallBack, buildSetStatusChangedEvent);
        });

        matchingTask.forEach((buildSetCallBack) -> {
            buildSetCallBack.callback(buildSetStatusChangedEvent);
        });
    }

    private void removeListenersOfCompletedTasks(BuildSetCallBack buildSetCallBack, BuildSetStatusChangedEvent buildSetStatusChangedEvent) {
        if (buildSetStatusChangedEvent.getNewStatus().isCompleted()) {
            subscribers.remove(buildSetCallBack);
        }
    }
}
