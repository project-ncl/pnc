package org.jboss.pnc.core.builder.operationHandlers;

import org.jboss.pnc.core.builder.BuildTask;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-03.
 */
public interface OperationHandler {
    void handle(BuildTask task);
    void next(OperationHandler handler);
}
