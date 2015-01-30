package org.jboss.pnc.rest.provider;

import static org.jboss.pnc.rest.provider.StreamHelper.nullableStreamOf;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.core.StreamingOutput;

import org.jboss.pnc.core.builder.BuildCoordinator;
import org.jboss.pnc.core.builder.BuildTask;
import org.jboss.pnc.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;

@Stateless
public class BuildRecordProvider extends BasePaginationProvider<BuildRecordRest, BuildRecord> {

    private BuildRecordRepository buildRecordRepository;
    private BuildCoordinator buildCoordinator;

    public BuildRecordProvider() {
    }

    @Inject
    public BuildRecordProvider(BuildRecordRepository buildRecordRepository, BuildCoordinator buildCoordinator) {
        this.buildRecordRepository = buildRecordRepository;
        this.buildCoordinator = buildCoordinator;
    }

    // Needed to map the Entity into the proper REST object
    @Override
    public Function<? super BuildRecord, ? extends BuildRecordRest> toRestModel() {
        return buildRecord -> new BuildRecordRest(buildRecord);
    }

    @Override
    public String getDefaultSortingField() {
        return BuildRecord.DEFAULT_SORTING_FIELD;
    }

    public Object getAllArchived(Integer pageIndex, Integer pageSize, String field, String sorting) {

        if (noPaginationRequired(pageIndex, pageSize, field, sorting)) {
            return buildRecordRepository.findAll().stream().map(toRestModel()).collect(Collectors.toList());
        } else {
            return transform(buildRecordRepository.findAll(buildPageRequest(pageIndex, pageSize, field, sorting)));
        }
    }

    public List<BuildRecordRest> getAllRunning() {
        return nullableStreamOf(buildCoordinator.getBuildTasks()).map(submittedBuild -> new BuildRecordRest(submittedBuild))
                .collect(Collectors.toList());
    }

    public List<BuildRecordRest> getAllArchivedOfBuildConfiguration(Integer buildRecordId) {
        return nullableStreamOf(buildRecordRepository.findByBuildConfigurationId(buildRecordId)).map(
                buildRecord -> new BuildRecordRest(buildRecord)).collect(Collectors.toList());
    }

    public BuildRecordRest getSpecific(Integer id) {
        BuildRecord buildRecord = buildRecordRepository.findOne(id);
        if (buildRecord != null) {
            return new BuildRecordRest(buildRecord);
        }
        return null;
    }

    public StreamingOutput getLogsForBuildId(Integer id) {
        BuildRecord buildRecord = buildRecordRepository.findOne(id);
        if (buildRecord != null) {
            return outputStream -> {
                Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream));
                writer.write(buildRecord.getBuildLog());
                writer.flush();
            };
        }
        return null;
    }

    public BuildRecordRest getSpecificRunning(Integer id) {
        BuildTask buildTask = getSubmittedBuild(id);
        if (buildTask != null) {
            return new BuildRecordRest(buildTask);
        }
        return null;
    }

    private BuildTask getSubmittedBuild(Integer id) {
        List<BuildTask> buildTasks = buildCoordinator.getBuildTasks().stream()
                .filter(submittedBuild -> id.equals(submittedBuild.getBuildConfiguration().getId()))
                .collect(Collectors.toList());
        if (!buildTasks.isEmpty()) {
            return buildTasks.iterator().next();
        }
        return null;
    }

    public StreamingOutput getLogsForRunningBuildId(Integer id) {
        BuildTask buildTask = getSubmittedBuild(id);
        if (buildTask != null) {
            return outputStream -> {
                Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream));
                if (buildTask != null && buildTask.getBuildLog() != null) {
                    writer.write(buildTask.getBuildLog());
                }
                writer.flush();
            };
        }
        return null;
    }
}
