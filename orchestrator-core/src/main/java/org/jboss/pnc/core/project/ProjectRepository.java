package org.jboss.pnc.core.project;

import org.jboss.pnc.core.buildinfo.model.BuildIdentifier;
import org.jboss.pnc.core.project.model.BuildRecipe;

public interface ProjectRepository {
    BuildRecipe getBuildRecepie(BuildIdentifier buildId);
}
