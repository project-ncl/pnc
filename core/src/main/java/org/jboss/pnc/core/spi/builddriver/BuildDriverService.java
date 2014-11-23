package org.jboss.pnc.core.spi.builddriver;

import org.jboss.pnc.model.BuildType;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
@Deprecated
public interface BuildDriverService {

    String getId();
    String getName();
    BuildType getBuildType();

}