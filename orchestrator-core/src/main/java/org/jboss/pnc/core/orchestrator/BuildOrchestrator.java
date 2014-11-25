package org.jboss.pnc.core.orchestrator;

import org.jboss.pnc.core.buildinfo.model.BuildIdentifier;
import org.jboss.pnc.core.buildinfo.model.BuildInfo;
import org.jboss.pnc.core.buildinfo.model.User;

/**
 * // TODO: Document this
 *
 * @author slaskawiec
 * @since 4.0
 */
public interface BuildOrchestrator {
    BuildInfo build(BuildIdentifier buildId, User whoStartedBuild);
    BuildInfo finishBuild(BuildIdentifier buildId, User whoFinishedBuild);
}
