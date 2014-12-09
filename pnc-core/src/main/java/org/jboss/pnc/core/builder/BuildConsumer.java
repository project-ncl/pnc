package org.jboss.pnc.core.builder;

import org.jboss.pnc.core.builder.operationHandlers.CompleteBuildHandler;
import org.jboss.pnc.core.builder.operationHandlers.ConfigureRepositoryHandler;
import org.jboss.pnc.core.builder.operationHandlers.StartBuildHandler;

import javax.inject.Inject;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-08.
 */
public class BuildConsumer implements Runnable {

    @Inject
    BuildQueue buildQueue;

    @Inject
    ConfigureRepositoryHandler configureRepositoryHandler;

    @Inject
    StartBuildHandler startBuildHandler;

    @Inject
    CompleteBuildHandler completeBuildHandler;

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
        configureRepositoryHandler.next(startBuildHandler);
        startBuildHandler.next(completeBuildHandler);

        configureRepositoryHandler.handle(buildTask);
    }
}
