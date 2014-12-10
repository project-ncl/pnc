package org.jboss.pnc.core.builder;

import javax.enterprise.context.ApplicationScoped;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-08.
 */
@ApplicationScoped
public class BuildQueue {

    private final BlockingQueue<BuildTask> queue = new LinkedBlockingDeque<>();

    public BuildTask take() throws InterruptedException {
        return queue.take();
    }

    public boolean add(BuildTask configuration) {
        return queue.add(configuration);
    }
}
