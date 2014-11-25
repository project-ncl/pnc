package org.jboss.pnc.core.trigger;

import org.jboss.pnc.core.buildinfo.model.BuildIdentifier;
import org.jboss.pnc.core.buildinfo.model.BuildInfo;

public interface BuildTrigger {

    BuildInfo startBuild(BuildIdentifier buildId);

}
