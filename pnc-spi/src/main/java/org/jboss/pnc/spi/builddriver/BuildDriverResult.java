package org.jboss.pnc.spi.builddriver;

import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.environment.RunningEnvironment;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-18.
 */
public interface BuildDriverResult {

    public String getBuildLog() throws BuildDriverException;

    public BuildDriverStatus getBuildDriverStatus() throws BuildDriverException;

    RunningEnvironment getRunningEnvironment();
}
