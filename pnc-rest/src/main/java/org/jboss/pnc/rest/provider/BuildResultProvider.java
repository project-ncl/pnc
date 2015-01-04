package org.jboss.pnc.rest.provider;

import org.jboss.pnc.core.builder.BuildCoordinator;
import org.jboss.pnc.core.builder.SubmittedBuild;
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
    private BuildCoordinator buildCoordinator;

    public BuildResultProvider() {
    }

    @Inject
    public BuildResultProvider(ProjectBuildResultRepository projectBuildResultRepository, BuildCoordinator buildCoordinator) {
        this.projectBuildResultRepository = projectBuildResultRepository;
        this.buildCoordinator = buildCoordinator;
    }

    public List<BuildResultRest> getAllArchived() {
        return nullableStreamOf(projectBuildResultRepository.findAll())
                .map(buildResult -> new BuildResultRest(buildResult)).collect(Collectors.toList());
    }

    public List<BuildResultRest> getAllRunning() {
        return nullableStreamOf(buildCoordinator.getSubmittedBuilds())
                .map(submittedBuild -> new BuildResultRest(submittedBuild)).collect(Collectors.toList());
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
        SubmittedBuild submittedBuild = getSubmittedBuild(id);
        if(submittedBuild != null) {
            return new BuildResultRest(submittedBuild);
        }
        return null;
    }

    private SubmittedBuild getSubmittedBuild(Integer id) {
        List<SubmittedBuild> submittedBuilds = buildCoordinator.getSubmittedBuilds().stream()
                    .filter(submittedBuild -> id.equals(submittedBuild.getProjectBuildConfiguration().getId()))
                    .collect(Collectors.toList());
        if(!submittedBuilds.isEmpty()) {
            return submittedBuilds.iterator().next();
        }
        return null;
    }

    public StreamingOutput getLogsForRunningBuildId(Integer id) {
        SubmittedBuild submittedBuild = getSubmittedBuild(id);
        if(submittedBuild != null) {
            return outputStream -> {
                Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream));
                if(submittedBuild != null && submittedBuild.getBuildLog() != null) {
                    writer.write(submittedBuild.getBuildLog());
                }
                writer.flush();
            };
        }
        return null;
    }
}
