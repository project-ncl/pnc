package org.jboss.pnc.rest.facade.mappers;

import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.rest.model.BuildConfigurationRef;
import org.jboss.pnc.rest.model.BuildConfigurationRest;

import java.time.Instant;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public class BuildConfigurationMapper extends AbstractMapper<BuildConfiguration, BuildConfigurationRest, BuildConfigurationRef> {

    private RepositoryConfigurationMapper repositoryConfigurationMapper;
    private ProjectMapper projectMapper;
    private EnvironmentMapper environmentMapper;
    private BuildTypeMapper buildTypeMapper; // won't need if enums are in some common module

    @Override
    public BuildConfigurationRest toRest(BuildConfiguration buildConfiguration) {
        if (buildConfiguration == null) {
            return null;
        }

        return BuildConfigurationRest.builder()
                .id(buildConfiguration.getId())
                .name(buildConfiguration.getName())
                .description(buildConfiguration.getDescription())
                .buildScript(buildConfiguration.getBuildScript())
                .scmRevision(buildConfiguration.getScmRevision())
                .creationTime(toInstant(buildConfiguration.getCreationTime()))
                .lastModificationTime(toInstant(buildConfiguration.getLastModificationTime()))
                .archived(buildConfiguration.isArchived())
                .genericParameters(toMap(buildConfiguration.getGenericParameters()))
                .repositoryConfiguration(repositoryConfigurationMapper
                        .toRef(buildConfiguration.getRepositoryConfiguration()))
                .project(projectMapper.toRef(buildConfiguration.getProject()))
                .environment(environmentMapper.toRef(buildConfiguration.getBuildEnvironment()))
                .dependencyIds(toIds(buildConfiguration.getDependencies()))
                .productVersionId(toId(buildConfiguration.getProductVersion()))
                .buildType(buildTypeMapper.map(buildConfiguration.getBuildType()))
                .build();
    }

    @Override
    public BuildConfigurationRef toRef(BuildConfiguration buildConfiguration) {
        if (buildConfiguration == null) {
            return null;
        }
        Instant i;


        return BuildConfigurationRef.builder()
                .id(buildConfiguration.getId())
                .name(buildConfiguration.getName())
                .description(buildConfiguration.getDescription())
                .buildScript(buildConfiguration.getBuildScript())
                .scmRevision(buildConfiguration.getScmRevision())
                .creationTime(toInstant(buildConfiguration.getCreationTime()))
                .lastModificationTime(toInstant(buildConfiguration.getLastModificationTime()))
                .archived(buildConfiguration.isArchived())
                .genericParameters(toMap(buildConfiguration.getGenericParameters()))
                .productVersionId(toId(buildConfiguration.getProductVersion()))
                .buildType(buildTypeMapper.map(buildConfiguration.getBuildType()))
                .build();
    }

    @Override
    public BuildConfiguration toEntity(BuildConfigurationRest buildConfiguration) {
        if (buildConfiguration == null) {
            return null;
        }

        BuildConfiguration.Builder builder = BuildConfiguration.Builder.newBuilder()
                .id(buildConfiguration.getId())
                .name(buildConfiguration.getName())
                .description(buildConfiguration.getDescription())
                .buildScript(buildConfiguration.getBuildScript())
                .scmRevision(buildConfiguration.getScmRevision())
                .archived(buildConfiguration.isArchived())
                .genericParameters(buildConfiguration.getGenericParameters())
                .buildType(buildTypeMapper.map(buildConfiguration.getBuildType()));
        // TODO do we want to map also refs to entities?
        return builder.build();
    }
}
