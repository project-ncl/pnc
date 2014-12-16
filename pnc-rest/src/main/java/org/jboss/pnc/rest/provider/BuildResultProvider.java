package org.jboss.pnc.rest.provider;

import org.jboss.pnc.core.builder.BuildTask;
import org.jboss.pnc.core.builder.ProjectBuilder;
import org.jboss.pnc.datastore.repositories.ProjectBuildResultRepository;
import org.jboss.pnc.model.ProjectBuildResult;
import org.jboss.pnc.rest.restmodel.BuildResultRest;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.core.StreamingOutput;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.provider.StreamHelper.nullableStreamOf;

@Stateless
public class BuildResultProvider {

    private ProjectBuildResultRepository projectBuildResultRepository;
    private ProjectBuilder projectBuilder;

    public BuildResultProvider() {
    }

    @Inject
    public BuildResultProvider(ProjectBuildResultRepository projectBuildResultRepository, ProjectBuilder projectBuilder) {
        this.projectBuildResultRepository = projectBuildResultRepository;
        this.projectBuilder = projectBuilder;
    }

    public List<BuildResultRest> getAllArchived() {
        return nullableStreamOf(projectBuildResultRepository.findAll())
                .map(buildResult -> new BuildResultRest(buildResult)).collect(Collectors.toList());
    }

    public List<BuildResultRest> getAllRunning() {
        return nullableStreamOf(projectBuilder.getRunningBuilds())
                .map(runningBuild -> new BuildResultRest(runningBuild)).collect(Collectors.toList());
    }

    public BuildResultRest getSpecific(Integer id) {
        ProjectBuildResult buildResult = projectBuildResultRepository.findOne(id);
        if(buildResult != null) {
            return new BuildResultRest(buildResult);
        }
        return null;
    }

    public StreamingOutput getLogsForBuildId(Integer id) {
        ProjectBuildResult buildResult = projectBuildResultRepository.findOne(id);
        if(buildResult != null) {
            return outputStream -> {
                Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream));
                writer.write(buildResult.getBuildLog());
                writer.flush();
            };
        }
        return null;
    }

    public BuildResultRest getSpecificRunning(Integer id) {
        BuildTask runningBuild = getRunningBuild(id);
        if(runningBuild != null) {
            return new BuildResultRest(runningBuild);
        }
        return null;
    }

    private BuildTask getRunningBuild(Integer id) {
        List<BuildTask> tasks = projectBuilder.getRunningBuilds().stream()
                    .filter(task -> id.equals(task.getId()))
                    .collect(Collectors.toList());
        if(!tasks.isEmpty()) {
            return tasks.iterator().next();
        }
        return null;
    }

    public StreamingOutput getLogsForRunningBuildId(Integer id) {
        BuildTask runningBuild = getRunningBuild(id);
        if(runningBuild != null) {
            return outputStream -> {
                Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream));
                if(runningBuild.getBuildJobDetails() != null && runningBuild.getBuildJobDetails().getBuildLog() != null) {
                    writer.write(runningBuild.getBuildJobDetails().getBuildLog());
                }
                writer.flush();
            };
        }
        return null;
    }
}
