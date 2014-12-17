package org.jboss.pnc.core.builder;

import org.jboss.pnc.core.builder.operationHandlers.ChainBuilder;
import org.jboss.pnc.core.builder.operationHandlers.CompleteHandler;
import org.jboss.pnc.core.builder.operationHandlers.ConfigureRepositoryHandler;
import org.jboss.pnc.core.builder.operationHandlers.ErrorStateHandler;
import org.jboss.pnc.core.builder.operationHandlers.OperationHandler;
import org.jboss.pnc.core.builder.operationHandlers.CollectResultsHandler;
import org.jboss.pnc.core.builder.operationHandlers.StartBuildHandler;
import org.jboss.pnc.core.builder.operationHandlers.WaitBuildToCompleteHandler;

import javax.inject.Inject;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-08.
 */
public class BuildConsumer implements Runnable {

    private final BuildTaskQueue buildTaskQueue;
    OperationHandler rootHandler;

    @Inject
    BuildConsumer(BuildTaskQueue buildTaskQueue,
                  ConfigureRepositoryHandler configureRepositoryHandler,
                  StartBuildHandler startBuildHandler,
                  WaitBuildToCompleteHandler waitBuildToCompleteHandler,
                  CollectResultsHandler collectResultsHandler,
                  CompleteHandler completeHandler,
                  ErrorStateHandler errorStateHandler) {

        this.buildTaskQueue = buildTaskQueue;

        rootHandler = new ChainBuilder()
            .addNext(configureRepositoryHandler)
            .addNext(startBuildHandler)
            .addNext(waitBuildToCompleteHandler)
            .addNext(collectResultsHandler)
            .addNext(completeHandler)
            .addNext(errorStateHandler)
            .build();
    }

    @Override
    public void run() {
        while (true) {
            try {
                BuildTask buildTask = buildTaskQueue.take();
                runBuildTask(buildTask);
            } catch (InterruptedException e) {
                break;
            }

        }
    }

    private void runBuildTask(BuildTask buildTask) {
        rootHandler.handle(buildTask);
    }
}
