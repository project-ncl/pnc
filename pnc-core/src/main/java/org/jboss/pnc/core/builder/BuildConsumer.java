package org.jboss.pnc.core.builder;

import org.jboss.pnc.core.builder.operationHandlers.ChainBuilder;
import org.jboss.pnc.core.builder.operationHandlers.CompleteBuildHandler;
import org.jboss.pnc.core.builder.operationHandlers.ConfigureRepositoryHandler;
import org.jboss.pnc.core.builder.operationHandlers.OperationHandler;
import org.jboss.pnc.core.builder.operationHandlers.StartBuildHandler;

import javax.inject.Inject;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-08.
 */
public class BuildConsumer implements Runnable {

    private final BuildQueue buildQueue;
    OperationHandler rootHandler;

    @Inject
    BuildConsumer(BuildQueue buildQueue,
                  ConfigureRepositoryHandler configureRepositoryHandler,
                  StartBuildHandler startBuildHandler,
                  CompleteBuildHandler completeBuildHandler) {

        this.buildQueue = buildQueue;

        rootHandler = new ChainBuilder()
            .addNext(configureRepositoryHandler)
            .addNext(startBuildHandler)
            .addNext(completeBuildHandler)
            .build();
    }

    @Override
    public void run() {
        while (true) {
            try {
                BuildTask buildTask = buildQueue.take();
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
