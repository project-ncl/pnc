package org.jboss.pnc.core.task;

import org.jboss.pnc.core.task.handlers.CheckBuildProgressHandler;
import org.jboss.pnc.core.task.handlers.CompleteBuildHandler;
import org.jboss.pnc.core.task.handlers.StartBuildHandler;
import org.jboss.pnc.model.exchange.Task;

import javax.enterprise.context.ApplicationScoped;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-03.
 */
@ApplicationScoped
public class TaskQueue {

    private final BlockingQueue<Task> queue = new LinkedBlockingDeque<>();

    TaskQueue() {
        CompleteBuildHandler completeBuildHandler = new CompleteBuildHandler();

        CheckBuildProgressHandler checkBuildProgressHandler = new CheckBuildProgressHandler();
        checkBuildProgressHandler.next(completeBuildHandler);

        StartBuildHandler startBuildHandler = new StartBuildHandler();
        startBuildHandler.next(checkBuildProgressHandler);
        new TaskConsumer(queue, startBuildHandler);
    }

    public void addTask(Task task) {
        queue.add(task);
    }



}
