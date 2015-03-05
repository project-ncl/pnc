package org.jboss.pnc.spi.builddriver;

import org.jboss.pnc.model.BuildDriverStatus;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.environment.RunningEnvironment;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-23.
 */
public interface CompletedBuild {
    BuildDriverStatus getCompleteStatus();

    BuildDriverResult getBuildResult() throws BuildDriverException;

    RunningEnvironment getRunningEnvironment();
}
