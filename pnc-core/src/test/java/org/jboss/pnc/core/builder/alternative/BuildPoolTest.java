package org.jboss.pnc.core.builder.alternative;

import org.jboss.pnc.model.BuildResult;
import org.jboss.pnc.model.Environment;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.builder.EnvironmentBuilder;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class BuildPoolTest {

    @Test
    public void shouldBuildProjectInProperOrder() throws Exception {
        //given
        BuildPool buildPool = new BuildPool(1);

        Environment notRelevant = EnvironmentBuilder.defaultEnvironment().build();

        Project p1 = new Project("p1", notRelevant);
        Project p2 = new Project("p2", notRelevant, p1);
        Project p3 = new Project("p3", notRelevant, p2);
        Project p4 = new Project("p4", notRelevant, p3, p1, p2);

        //when
        BlockingQueue<Future<BuildResult>> builds = buildPool.submit((project) -> {
                BuildResult buildResult = new BuildResult();
                buildResult.setProject(project);
                return buildResult;
            }, p4);

        //then
        assertThat(getBuildResults(builds)).containsExactly(p1, p2, p3, p4);
    }

    private List<Project> getBuildResults(BlockingQueue<Future<BuildResult>> workLog) {
        return workLog.stream()
                .map((future) -> {
                    try {
                        return future.get(10, TimeUnit.MILLISECONDS).getProject();
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                })
                .collect(Collectors.toList());
    }

}