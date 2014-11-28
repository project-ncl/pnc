package org.jboss.pnc.core.builder.alternative;

import org.jboss.pnc.core.builder.alternative.recipe.MavenBuildRecipe;
import org.jboss.pnc.model.BuildResult;
import org.jboss.pnc.model.Project;

import javax.inject.Inject;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.function.Function;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
public class BuildCoordinator {

    private final BuildPool buildPool;

    @Inject
    public BuildCoordinator(BuildPool buildPool) {
        this.buildPool = buildPool;
    }

    public void buildProjects(Project... projects) {
        BlockingQueue<Future<BuildResult>> builds = buildPool.submit(getRecipe(projects), projects);
        // do something with builds...
    }

    public Function<Project, BuildResult> getRecipe(Project... projects) {
        //make some sophisticated decision
        return new MavenBuildRecipe();
    }


}
