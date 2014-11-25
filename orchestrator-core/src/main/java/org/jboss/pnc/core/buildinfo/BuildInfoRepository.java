package org.jboss.pnc.core.buildinfo;

import org.jboss.pnc.core.buildinfo.model.BuildIdentifier;
import org.jboss.pnc.core.buildinfo.model.BuildInfo;

import java.util.List;

public interface BuildInfoRepository {
    BuildInfo save(BuildInfo build);
    BuildInfo findByBuildId(BuildIdentifier buildId);
    List<BuildInfo> findRunningBuilds();
}
