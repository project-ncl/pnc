package org.jboss.pnc.rest.trigger;

import org.jboss.pnc.core.buildinfo.model.BuildIdentifier;
import org.jboss.pnc.core.buildinfo.model.BuildInfo;
import org.jboss.pnc.core.orchestrator.BuildOrchestrator;
import org.jboss.pnc.core.trigger.BuildTrigger;
import org.jboss.pnc.rest.authorization.AuthorizationService;

public class RestBuildTrigger implements BuildTrigger {

    BuildOrchestrator orchestrator;
    AuthorizationService authorization;

    @Override
    public BuildInfo startBuild(BuildIdentifier buildId) {
        return orchestrator.build(buildId, authorization.getCaller());
    }
}
