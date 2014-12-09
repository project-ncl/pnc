package org.jboss.pnc.core.builder.handlers;

import org.jboss.pnc.core.builder.BuildTask;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-03.
 */
public interface Handler {
    void handle(BuildTask task);
    void next(Handler handler);
}
