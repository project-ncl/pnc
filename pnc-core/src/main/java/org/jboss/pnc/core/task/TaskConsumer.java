package org.jboss.pnc.core.task;

import org.jboss.pnc.core.task.handlers.Handler;
import org.jboss.pnc.model.TaskStatus;
import org.jboss.pnc.model.exchange.Task;

import java.util.concurrent.BlockingQueue;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-03.
 */
public class TaskConsumer implements Runnable {

    private final BlockingQueue<Task> queue;
    private Handler rootHandler;

    TaskConsumer(BlockingQueue queue, Handler rootHandler) {
        this.queue = queue;
        this.rootHandler = rootHandler;
    }

    public void run() {
        try {
            while (true) {
                consume(queue.take());
            }
        } catch (InterruptedException ex) {
            //TODO
        }
    }
    void consume(Task task) {
        rootHandler.handle(task);
        if (!TaskStatus.Operation.COMPLETED.equals(task.getStatus())) {
            queue.add(task);
        }
    }
}
