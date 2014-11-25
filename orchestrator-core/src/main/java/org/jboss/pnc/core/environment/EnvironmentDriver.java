package org.jboss.pnc.core.environment;

import org.jboss.pnc.core.buildinfo.model.BuildInfo;
import org.jboss.pnc.core.environment.model.EnvironmentRecipe;
import org.jboss.pnc.core.environment.model.RunnableEnvironment;

public interface EnvironmentDriver {
    RunnableEnvironment buildEnvironment(EnvironmentRecipe environmentRecipe);
    void cleanupEnvironment(BuildInfo buildInfo);
}
