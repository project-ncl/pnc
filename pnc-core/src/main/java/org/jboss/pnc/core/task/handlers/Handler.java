package org.jboss.pnc.core.task.handlers;

import org.jboss.pnc.model.exchange.Task;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-03.
 */
public interface Handler {
    void handle(Task task);
    void next(Handler handler);
}
