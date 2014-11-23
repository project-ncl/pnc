package org.jboss.pnc.jenkinsbuilddriver;

import org.jboss.pnc.core.model.BuildResult;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
//TODO
public class BuildResultFuture implements Future<BuildResult> {
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public BuildResult get() throws InterruptedException, ExecutionException {
        return null;
    }

    @Override
    public BuildResult get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }
}
