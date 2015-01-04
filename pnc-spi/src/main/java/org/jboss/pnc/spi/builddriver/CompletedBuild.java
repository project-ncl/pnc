package org.jboss.pnc.spi.builddriver;

import org.jboss.pnc.model.BuildDriverStatus;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-23.
 */
public interface CompletedBuild {
    BuildDriverStatus getCompleteStatus();

    BuildResult getBuildResult() throws BuildDriverException;
}
