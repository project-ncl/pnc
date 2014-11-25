package org.jboss.pnc.core.environment;

import org.jboss.pnc.core.environment.model.EnvironmentRecipe;
import org.jboss.pnc.core.project.model.BuildRecipe;

public interface EnvironmentRepository {
    EnvironmentRecipe getEnvironmentRecipe(BuildRecipe buildRecipe);
}
