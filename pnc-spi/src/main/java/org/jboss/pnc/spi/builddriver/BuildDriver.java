package org.jboss.pnc.spi.builddriver;

import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.model.exchange.Task;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
public interface BuildDriver<T> {

    String getDriverId();

    boolean canBuild(BuildType buildType);

    /**
     * Method returns as soon as build was triggered.
     *
     * @return return false if driver is not ready for accepting new requests
     */
    boolean startProjectBuild(Task<T> buildTask);
}
