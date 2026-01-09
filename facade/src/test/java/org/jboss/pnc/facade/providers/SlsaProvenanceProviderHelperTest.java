/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.facade.providers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.api.enums.slsa.BuildSystem;
import org.jboss.pnc.api.slsa.dto.provenance.v1.BuildDefinition;
import org.jboss.pnc.api.slsa.dto.provenance.v1.Builder;
import org.jboss.pnc.api.slsa.dto.provenance.v1.Metadata;
import org.jboss.pnc.api.slsa.dto.provenance.v1.Provenance;
import org.jboss.pnc.api.slsa.dto.provenance.v1.ResourceDescriptor;
import org.jboss.pnc.api.slsa.dto.provenance.v1.RunDetails;
import org.jboss.pnc.common.Strings;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.json.moduleconfig.slsa.BuilderConfig;
import org.jboss.pnc.common.json.moduleconfig.slsa.ProvenanceEntry;
import org.jboss.pnc.constants.ReposiotryIdentifier;
import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfigurationRevision;
import org.jboss.pnc.dto.Environment;
import org.jboss.pnc.dto.Project;
import org.jboss.pnc.dto.SCMRepository;
import org.jboss.pnc.dto.TargetRepository;
import org.jboss.pnc.dto.User;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.BuildCategory;
import org.jboss.pnc.enums.BuildProgress;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.enums.BuildType;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.enums.SystemImageType;
import org.jboss.pnc.facade.util.SlsaProvenanceUtils;
import org.jboss.pnc.spi.datastore.repositories.ArtifactAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import static org.jboss.pnc.api.constants.slsa.ProvenanceKeys.*;

/**
 *
 * @author <a href="mailto:andrea.vibelli@gmail.com">Andrea Vibelli</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class SlsaProvenanceProviderHelperTest extends AbstractIntIdProviderTest<org.jboss.pnc.model.Artifact> {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Mock
    private ArtifactRepository artifactRepository;

    @Mock
    private ArtifactAuditedRepository artifactAuditedRepository;

    @Mock
    private BuildRecordRepository buildRecordRepository;

    @Spy
    @InjectMocks
    private SlsaProvenanceProviderHelper providerHelper;

    @Spy
    @InjectMocks
    private ArtifactProviderImpl provider;

    private final List<org.jboss.pnc.model.Artifact> artifacts = new ArrayList<>();
    private final org.jboss.pnc.model.Artifact importedArtifact = createArtifact(
            null,
            "foo",
            "bar",
            "1.0.0",
            "abc1234a");
    private final org.jboss.pnc.model.Artifact builtArtifact = createArtifact(
            "BMGAM3BGEWYAA",
            "foo",
            "bar",
            "1.0.0",
            "abc1234a");

    @Override
    protected AbstractProvider provider() {
        return provider;
    }

    @Override
    protected Repository repository() {
        return artifactRepository;
    }

    @Before
    public void prepareMocks() throws ReflectiveOperationException {

    }

    public SlsaProvenanceProviderHelperTest() {
        artifacts.add(importedArtifact);
    }

    @Test
    public void testFindArtifactById() {
        when(artifactRepository.queryById(importedArtifact.getId())).thenReturn(importedArtifact);
        Artifact found = providerHelper.getArtifactById(Integer.toString(importedArtifact.getId()));

        assertNotNull(found);
        assertThat(found.getId()).isEqualTo(importedArtifact.getId().toString());
        assertThat(found.getIdentifier()).isEqualTo(importedArtifact.getIdentifier());
        assertThat(found.getSha256()).isEqualTo(importedArtifact.getSha256());
    }

    @Test
    public void testFindArtifactByPurl() {
        when(artifactRepository.withPurl(importedArtifact.getPurl())).thenReturn(importedArtifact);

        Artifact found = providerHelper.getArtifactByPurl(importedArtifact.getPurl());

        assertNotNull(found);
        assertThat(found.getId()).isEqualTo(importedArtifact.getId().toString());
        assertThat(found.getIdentifier()).isEqualTo(importedArtifact.getIdentifier());
        assertThat(found.getSha256()).isEqualTo(importedArtifact.getSha256());
    }

    @Test
    public void testFindArtifactByDigest() {
        fillRepository(List.of(importedArtifact));
        when(repository().queryWithPredicates(any(Predicate[].class))).thenReturn(repositoryList);

        List<Artifact> found = providerHelper.getAllArtifactsByDigest(
                importedArtifact.getSha256(),
                importedArtifact.getMd5(),
                importedArtifact.getSha1());

        assertThat(found.size()).isEqualTo(1);
        assertThat(found.get(0).getId()).isEqualTo(importedArtifact.getId().toString());
        assertThat(found.get(0).getIdentifier()).isEqualTo(importedArtifact.getIdentifier());
        assertThat(found.get(0).getSha256()).isEqualTo(importedArtifact.getSha256());
    }

    @Test
    public void testFindAllBuiltArtifacts() {
        fillRepository(List.of(builtArtifact));
        when(repository().queryWithPredicates(any(Predicate[].class))).thenReturn(repositoryList);

        Collection<Artifact> found = providerHelper
                .getAllBuiltArtifacts(Build.builder().id(Integer.toString(builtArtifact.getId())).build());
        assertThat(found.size()).isEqualTo(1);
    }

    @Test
    public void testMissArtifactById() {
        when(artifactRepository.queryById(importedArtifact.getId())).thenReturn(importedArtifact);

        try {
            providerHelper.getArtifactById("123321");
            fail("Should have thrown not found exception");
        } catch (Exception ex) {
            // ok
        }
    }

    @Test
    public void testMissArtifactByPurl() {
        when(artifactRepository.withPurl(importedArtifact.getPurl())).thenReturn(importedArtifact);

        try {
            providerHelper.getArtifactByPurl("missing_purl");
            fail("Should have thrown not found exception");
        } catch (Exception ex) {
            // ok
        }
    }

    @Test
    public void testMissArtifactByDigest() {
        when(repository().queryWithPredicates(any(Predicate[].class))).thenReturn(List.of());
        List<Artifact> found = providerHelper.getAllArtifactsByDigest("sha256", "md5", "sha1");
        assertNotNull(found);
        assertThat(found.size()).isEqualTo(0);
    }

    @Test
    public void testProvenanceCreation() {

        // Initialize all the required data to create a provenance
        BuildConfigurationRevision buildConfigRevision = initBuildConfigurationRevision();
        Build build = initBuild(buildConfigRevision);
        TargetRepository targetRepository = iniTargetRepository();
        Collection<Artifact> builtArtifacts = initBuiltArtifacts(build, targetRepository);
        Collection<Artifact> dependencyArtifacts = initDependenciesArtifacts(targetRepository);

        ProvenanceEntry builder = createProvenanceEntry("id", "externalPncUrl", "/builds/${buildId}");
        List<ProvenanceEntry> componentVersions = createComponentVersions();
        List<ProvenanceEntry> byProducts = createByProducts();

        BuilderConfig builderConfig = new BuilderConfig(builder, componentVersions, byProducts);

        GlobalModuleGroup globalConfig = new GlobalModuleGroup();
        globalConfig.setExternalPncUrl("https://orch-stage.redhat.com/pnc-rest/v2");
        globalConfig.setExternalEnvironmentDriverUrl("https://environment-stage.redhat.com");
        globalConfig.setExternalKafkaStoreUrl("https://kafka-store-stage.redhat.com");

        SlsaProvenanceUtils slsaProvenanceUtils = new SlsaProvenanceUtils(
                build,
                buildConfigRevision,
                builtArtifacts,
                dependencyArtifacts,
                builderConfig,
                globalConfig,
                providerHelper::getBodyFromHttpRequest);

        // Generate the provenance
        Provenance provenance = slsaProvenanceUtils.createBuildProvenance();

        // Test `predicateType` and `_type`
        assertNotNull(provenance);
        assertThat(provenance.getPredicateType()).isEqualTo(SlsaProvenanceUtils.SLSLA_BUILD_PROVENANCE_PREDICATE_TYPE);
        assertThat(provenance.getType()).isEqualTo(SlsaProvenanceUtils.SLSLA_BUILD_PROVENANCE_ATTESTATION_TYPE);

        // Test `subject`
        assertThat(provenance.getSubject().size()).isEqualTo(3);
        ResourceDescriptor subject1 = provenance.getSubject().get(0);
        Artifact builtArtifact1 = builtArtifacts.iterator().next();
        assertThat(subject1.getAnnotations().size()).isEqualTo(3);
        assertThat(subject1.getAnnotations().containsKey(PROVENANCE_V1_ARTIFACT_IDENTIFIER)).isTrue();
        assertThat(subject1.getAnnotations().containsKey(PROVENANCE_V1_ARTIFACT_PURL)).isTrue();
        assertThat(subject1.getAnnotations().containsKey(PROVENANCE_V1_ARTIFACT_URI)).isTrue();
        assertThat(subject1.getDigest().containsKey(PROVENANCE_V1_ARTIFACT_SHA256)).isTrue();
        assertThat(subject1.getAnnotations().get(PROVENANCE_V1_ARTIFACT_IDENTIFIER))
                .isEqualTo(builtArtifact1.getIdentifier());
        assertThat(subject1.getAnnotations().get(PROVENANCE_V1_ARTIFACT_PURL)).isEqualTo(builtArtifact1.getPurl());
        assertThat(subject1.getAnnotations().get(PROVENANCE_V1_ARTIFACT_URI)).isEqualTo(builtArtifact1.getPublicUrl());
        assertThat(subject1.getDigest().get(PROVENANCE_V1_ARTIFACT_SHA256)).isEqualTo(builtArtifact1.getSha256());
        assertThat(subject1.getName()).isEqualTo(builtArtifact1.getFilename());

        // Test `predicate`.`runDetails`
        RunDetails runDetails = provenance.getPredicate().getRunDetails();

        // Test `predicate`.`runDetails`.`metadata`
        Metadata metadata = runDetails.getMetadata();
        assertThat(metadata.getInvocationId()).isEqualTo(builtArtifact1.getBuild().getId());
        assertThat(metadata.getStartedOn()).isEqualTo(builtArtifact1.getBuild().getSubmitTime());
        assertThat(metadata.getFinishedOn()).isEqualTo(builtArtifact1.getBuild().getEndTime());
        assertThat(runDetails.getByproducts().size()).isEqualTo(2);

        // Test `predicate`.`runDetails`.`byproducts`
        ResourceDescriptor buildLog = runDetails.getByproducts()
                .stream()
                .filter(descriptor -> descriptor.getName().equals(PROVENANCE_V1_BY_PRODUCTS_BUILD_LOG))
                .findFirst()
                .orElse(null);
        ResourceDescriptor alignmentLog = runDetails.getByproducts()
                .stream()
                .filter(descriptor -> descriptor.getName().equals(PROVENANCE_V1_BY_PRODUCTS_ALIGNMENT_LOG))
                .findFirst()
                .orElse(null);
        assertNotNull(buildLog);
        assertNotNull(alignmentLog);
        assertThat(buildLog.getUri()).isEqualTo(
                "https://orch-stage.redhat.com/pnc-rest/v2/builds/" + builtArtifact1.getBuild().getId()
                        + "/logs/build");
        assertThat(alignmentLog.getUri()).isEqualTo(
                "https://orch-stage.redhat.com/pnc-rest/v2/builds/" + builtArtifact1.getBuild().getId()
                        + "/logs/align");

        // Test `predicate`.`runDetails`.`builder`
        Builder slsaBuilder = runDetails.getBuilder();
        assertThat(slsaBuilder.getId())
                .isEqualTo("https://orch-stage.redhat.com/pnc-rest/v2/builds/" + builtArtifact1.getBuild().getId());
        assertThat(slsaBuilder.getVersion().size()).isEqualTo(3);
        assertThat(slsaBuilder.getVersion().containsKey("https://github.com/project-ncl/environment-driver")).isTrue();
        assertThat(slsaBuilder.getVersion().containsKey("https://github.com/project-ncl/kafka-store")).isTrue();
        assertThat(slsaBuilder.getVersion().containsKey("https://github.com/project-ncl/pnc")).isTrue();
        assertThat(slsaBuilder.getVersion().get("https://github.com/project-ncl/environment-driver"))
                .isEqualTo("https://environment-stage.redhat.com/version");
        assertThat(slsaBuilder.getVersion().get("https://github.com/project-ncl/kafka-store"))
                .isEqualTo("https://kafka-store-stage.redhat.com/version");
        assertThat(slsaBuilder.getVersion().get("https://github.com/project-ncl/pnc"))
                .isEqualTo("https://orch-stage.redhat.com/pnc-rest/v2/version");

        // Test `predicate`.`buildDefinition`
        BuildDefinition buildDefinition = provenance.getPredicate().getBuildDefinition();

        // Test `predicate`.`buildDefinition`.`buildType`
        assertThat(buildDefinition.getBuildType()).isEqualTo(BuildSystem.PNC.getBuildType());

        // Test `predicate`.`buildDefinition`.`externalParameters`
        Map<String, Object> externalParams = buildDefinition.getExternalParameters();

        // Test `predicate`.`buildDefinition`.`externalParameters`.`build`
        assertThat(externalParams.containsKey(PROVENANCE_V1_BUILD)).isTrue();
        Map<String, Object> buildParametersEntry = (Map<String, Object>) externalParams.get(PROVENANCE_V1_BUILD);
        assertThat(buildParametersEntry.containsKey(PROVENANCE_V1_BUILD_DETAILS_TYPE)).isTrue();
        assertThat(buildParametersEntry.containsKey(PROVENANCE_V1_BUILD_DETAILS_TEMPORARY)).isTrue();
        assertThat(buildParametersEntry.containsKey(PROVENANCE_V1_BUILD_DETAILS_SCRIPT)).isTrue();
        assertThat(buildParametersEntry.containsKey(PROVENANCE_V1_BUILD_DETAILS_NAME)).isTrue();
        assertThat(buildParametersEntry.containsKey(PROVENANCE_V1_BUILD_DETAILS_PARAMETERS)).isTrue();
        assertThat(buildParametersEntry.get(PROVENANCE_V1_BUILD_DETAILS_TYPE))
                .isEqualTo(buildConfigRevision.getBuildType().toString());
        assertThat(buildParametersEntry.get(PROVENANCE_V1_BUILD_DETAILS_TEMPORARY))
                .isEqualTo(String.valueOf(build.getTemporaryBuild()));
        assertThat(buildParametersEntry.get(PROVENANCE_V1_BUILD_DETAILS_SCRIPT))
                .isEqualTo(buildConfigRevision.getBuildScript());
        assertThat(buildParametersEntry.get(PROVENANCE_V1_BUILD_DETAILS_NAME)).isEqualTo(buildConfigRevision.getName());
        Map<String, Object> mergedParams = (Map<String, Object>) buildParametersEntry
                .get(PROVENANCE_V1_BUILD_DETAILS_PARAMETERS);
        assertThat(mergedParams.get(PROVENANCE_V1_BUILD_DETAILS_BREW_PULL_ACTIVE))
                .isEqualTo(String.valueOf(buildConfigRevision.isBrewPullActive()));

        // Test `predicate`.`buildDefinition`.`externalParameters`.`environment`
        assertThat(externalParams.containsKey(PROVENANCE_V1_ENVIRONMENT)).isTrue();
        Map<String, Object> envParams = (Map<String, Object>) externalParams.get(PROVENANCE_V1_ENVIRONMENT);
        assertThat(envParams.get(PROVENANCE_V1_NAME)).isEqualTo(buildConfigRevision.getEnvironment().getName());

        // Test `predicate`.`buildDefinition`.`externalParameters`.`repository`
        assertThat(externalParams.containsKey(PROVENANCE_V1_SCM_REPOSITORY)).isTrue();
        Map<String, Object> scmParams = (Map<String, Object>) externalParams.get(PROVENANCE_V1_SCM_REPOSITORY);
        assertThat(scmParams.get(PROVENANCE_V1_URI)).isEqualTo(build.getScmRepository().getExternalUrl());
        assertThat(scmParams.get(PROVENANCE_V1_REVISION)).isEqualTo(buildConfigRevision.getScmRevision());
        assertThat(scmParams.get(PROVENANCE_V1_PRE_BUILD_SYNC))
                .isEqualTo(String.valueOf(buildConfigRevision.getScmRepository().getPreBuildSyncEnabled()));

        // Test `predicate`.`buildDefinition`.`internalParameters`
        Map<String, Object> internalParams = buildDefinition.getInternalParameters();
        assertThat(internalParams.containsKey(PROVENANCE_V1_BUILD_DETAILS_DEFAULT_ALIGN_PARAMETERS)).isTrue();
        assertThat(internalParams.get(PROVENANCE_V1_BUILD_DETAILS_DEFAULT_ALIGN_PARAMETERS))
                .isEqualTo(buildConfigRevision.getDefaultAlignmentParams());

        // Test `predicate`.`buildDefinition`.`resolvedDependencies`
        List<ResourceDescriptor> resolvedDependencies = buildDefinition.getResolvedDependencies();

        // Test `repository`
        ResourceDescriptor repository = resolvedDependencies.stream()
                .filter(descriptor -> descriptor.getName().equals(PROVENANCE_V1_SCM_REPOSITORY))
                .findFirst()
                .orElse(null);
        assertNotNull(repository);
        assertThat(repository.getDigest().containsKey(PROVENANCE_V1_SCM_COMMIT)).isTrue();
        assertThat(repository.getDigest().get(PROVENANCE_V1_SCM_COMMIT)).isEqualTo(build.getScmBuildConfigRevision());
        assertThat(repository.getUri()).isEqualTo(build.getScmRepository().getExternalUrl());

        // Test `repository.downstream`
        ResourceDescriptor downstreamRepository = resolvedDependencies.stream()
                .filter(descriptor -> descriptor.getName().equals(PROVENANCE_V1_SCM_DOWNSTREAM_REPOSITORY))
                .findFirst()
                .orElse(null);
        assertNotNull(downstreamRepository);
        assertThat(downstreamRepository.getDigest().containsKey(PROVENANCE_V1_SCM_COMMIT)).isTrue();
        assertThat(downstreamRepository.getDigest().get(PROVENANCE_V1_SCM_COMMIT)).isEqualTo(build.getScmRevision());
        assertThat(downstreamRepository.getUri()).isEqualTo(build.getScmUrl());
        assertThat(downstreamRepository.getAnnotations().containsKey(PROVENANCE_V1_SCM_TAG)).isTrue();
        assertThat(downstreamRepository.getAnnotations().get(PROVENANCE_V1_SCM_TAG)).isEqualTo(build.getScmTag());

        // Test `environment`
        ResourceDescriptor env = resolvedDependencies.stream()
                .filter(descriptor -> descriptor.getName().equals(PROVENANCE_V1_ENVIRONMENT))
                .findFirst()
                .orElse(null);
        assertNotNull(env);
        assertThat(env.getUri()).isEqualTo(
                build.getEnvironment().getSystemImageRepositoryUrl() + "/" + build.getEnvironment().getSystemImageId());

        // Test the dependencies
        for (Artifact artifact : dependencyArtifacts) {
            ResourceDescriptor dependency = resolvedDependencies.stream()
                    .filter(descriptor -> descriptor.getName().equals(artifact.getFilename()))
                    .findFirst()
                    .orElse(null);
            assertNotNull(dependency);
            Map<String, Object> annotations = dependency.getAnnotations();
            assertThat(annotations.containsKey(PROVENANCE_V1_ARTIFACT_IDENTIFIER)).isTrue();
            assertThat(annotations.containsKey(PROVENANCE_V1_ARTIFACT_PURL)).isTrue();
            assertThat(annotations.containsKey(PROVENANCE_V1_ARTIFACT_URI)).isTrue();
            assertThat(annotations.get(PROVENANCE_V1_ARTIFACT_IDENTIFIER)).isEqualTo(artifact.getIdentifier());
            assertThat(annotations.get(PROVENANCE_V1_ARTIFACT_PURL)).isEqualTo(artifact.getPurl());
            assertThat(annotations.get(PROVENANCE_V1_ARTIFACT_URI)).isEqualTo(artifact.getPublicUrl());
            assertThat(dependency.getDigest().containsKey(PROVENANCE_V1_ARTIFACT_SHA256)).isTrue();
            assertThat(dependency.getDigest().get(PROVENANCE_V1_ARTIFACT_SHA256)).isEqualTo(artifact.getSha256());
        }
    }

    private org.jboss.pnc.model.Artifact createArtifact(
            String buildId,
            String groupId,
            String artifactId,
            String version,
            String checksum) {

        org.jboss.pnc.model.Artifact.Builder artifactBuilder = org.jboss.pnc.model.Artifact.builder()
                .id(getNextId())
                .artifactQuality(ArtifactQuality.NEW)
                .identifier(groupId + ":" + artifactId + ":" + version)
                .md5("md5-" + checksum)
                .sha1("sha1-" + checksum)
                .sha256("sha256-" + checksum)
                .purl("pkg:maven/" + groupId + "/" + artifactId + "@" + version + "?type=jar");
        if (!Strings.isEmpty(buildId)) {
            artifactBuilder.buildRecord(org.jboss.pnc.model.BuildRecord.Builder.newBuilder().id(buildId).build());
        }
        return artifactBuilder.build();
    }

    private BuildConfigurationRevision initBuildConfigurationRevision() {
        SCMRepository scmRepository = SCMRepository.builder()
                .id(String.valueOf(getNextId()))
                .internalUrl("git+ssh://code.stage.engineering.redhat.com/vibe13/sentinel.git")
                .externalUrl("https://github.com/vibe13/sentinel.git")
                .preBuildSyncEnabled(true)
                .build();

        Project project = Project.builder()
                .description("Generation of SLSA Provenance for Project Newcastle built artfacts")
                .engineeringTeam(null)
                .id(String.valueOf(getNextId()))
                .name("sentinel")
                .projectUrl("https://github.com/vibe13/sentinel")
                .build();

        Environment environment = Environment.builder()
                .attributes(Map.of("JDK", "1.7.0", "OS", "Linux"))
                .deprecated(false)
                .description("Basic Java and Maven Environment")
                .hidden(false)
                .id(String.valueOf(getNextId()))
                .name("Environment 1")
                .systemImageId("12345678")
                .systemImageRepositoryUrl("my.registry/newcastle")
                .systemImageType(SystemImageType.DOCKER_IMAGE)
                .build();

        User user = User.builder().username("demo-user").build();

        BuildConfigurationRevision buildConfigRevision = BuildConfigurationRevision.builder()
                .brewPullActive(false)
                .buildScript("mvn deploy -DskipTests=true")
                .buildType(BuildType.MVN)
                .creationTime(Instant.now())
                .defaultAlignmentParams(
                        "-DdependencySource=REST -DrepoRemovalBackup=repositories-backup.xml -DversionSuffixStrip= -DreportNonAligned=true -DstrictPropertyValidation=true")
                .id(String.valueOf(getNextId()))
                .modificationTime(Instant.now())
                .name("Test project for SLSA Provenance generation")
                .rev(getNextId())
                .scmRevision("*/v0.2")
                .creationUser(user)
                .environment(environment)
                .modificationUser(user)
                .parameters(
                        Map.of(
                                "ALIGNMENT_PARAMETERS",
                                "-DdependencyOverride.io.grpc:grpc-bom@*= -DdependencyOverride.com.google.protobuf:protobuf-java@*="))
                .project(project)
                .scmRepository(scmRepository)
                .build();

        return buildConfigRevision;
    }

    private Build initBuild(BuildConfigurationRevision buildConfigRevision) {
        Integer id = getNextId();

        Build build = Build.builder()
                .alignmentPreference(AlignmentPreference.PREFER_PERSISTENT)
                .attributes(
                        Map.of("BREW_BUILD_NAME", "org.jboss.pnc:parent", "BREW_BUILD_VERSION", "1.2.3", "FOO", "bar"))
                .buildConfigRevision(buildConfigRevision)
                .buildContentId("build-" + id)
                .endTime(Instant.now())
                .environment(buildConfigRevision.getEnvironment())
                .id(Integer.toString(id))
                .progress(BuildProgress.FINISHED)
                .project(buildConfigRevision.getProject())
                .scmBuildConfigRevision(buildConfigRevision.getScmRevision())
                .scmBuildConfigRevisionInternal(false)
                .scmRepository(buildConfigRevision.getScmRepository())
                .scmRevision(buildConfigRevision.getScmRevision())
                .scmTag(buildConfigRevision.getScmRepository().getInternalUrl() + ".pnc-tag-" + id)
                .scmUrl(buildConfigRevision.getScmRepository().getExternalUrl())
                .startTime(Instant.now().minus(5, ChronoUnit.MINUTES))
                .status(BuildStatus.SUCCESS)
                .submitTime(Instant.now().minus(8, ChronoUnit.MINUTES))
                .temporaryBuild(false)
                .user(buildConfigRevision.getCreationUser())
                .build();

        return build;
    }

    private TargetRepository iniTargetRepository() {
        return TargetRepository.refBuilder()
                .repositoryType(RepositoryType.MAVEN)
                .repositoryPath("builds-untested")
                .identifier(ReposiotryIdentifier.INDY_MAVEN)
                .temporaryRepo(false)
                .build();
    }

    private Artifact initArtifact(
            Build build,
            TargetRepository targetRepository,
            String groupId,
            String artifactId,
            String version,
            String classifier) {

        Artifact.Builder artifactBuilder = Artifact.builder()
                .artifactQuality(ArtifactQuality.NEW)
                .buildCategory(BuildCategory.STANDARD)
                .creationTime(Instant.now())
                .deployPath(
                        String.format(
                                "%s/%s/%s/%s-%s.%s",
                                groupId,
                                artifactId,
                                version,
                                artifactId,
                                version,
                                classifier))
                .filename(String.format("%s-%s.%s", artifactId, version, classifier))
                .identifier(String.format("%s:%s:%s:%s", groupId, artifactId, classifier, version))
                .originUrl(String.format("http://indy/%s-%s.%s", artifactId, version, classifier))
                .purl(String.format("pkg:maven/%s/%s@%s?type=%s", groupId, artifactId, version, classifier))
                .publicUrl(
                        String.format(
                                "https://indy.com/api/content/maven/hosted/pnc-builds/%s/%s/%s/%s-%s.%s",
                                groupId.replaceAll("\\.", "/"),
                                artifactId,
                                version,
                                artifactId,
                                version,
                                classifier))
                .id(String.valueOf(getNextId()))
                .md5(randomHex(16))
                .sha1(randomHex(20))
                .sha256(randomHex(32))
                .size(RANDOM.nextLong())
                .targetRepository(targetRepository);

        if (build != null) {
            artifactBuilder.build(build).creationUser(build.getUser());
        } else {
            artifactBuilder.importDate(Instant.now());
        }
        return artifactBuilder.build();
    }

    public List<Artifact> initBuiltArtifacts(Build build, TargetRepository targetRepository) {
        Artifact builtArtifact1 = initArtifact(build, targetRepository, "org.jboss.pnc", "parent", "1.2.3", "pom");
        Artifact builtArtifact2 = initArtifact(build, targetRepository, "org.jboss.pnc", "dto", "1.2.3", "pom");
        Artifact builtArtifact3 = initArtifact(build, targetRepository, "org.jboss.pnc", "dto", "1.2.3", "jar");
        return List.of(builtArtifact1, builtArtifact2, builtArtifact3);
    }

    public List<Artifact> initDependenciesArtifacts(TargetRepository targetRepository) {
        Artifact dependency1 = initArtifact(
                null,
                targetRepository,
                "com.google.http-client",
                "google-http-client-bom",
                "1.46.1",
                "pom");
        Artifact dependency2 = initArtifact(
                null,
                targetRepository,
                "com.google.http-client",
                "google-http-client-apache-v2",
                "1.46.1",
                "pom");
        Artifact dependency3 = initArtifact(
                null,
                targetRepository,
                "com.google.http-client",
                "google-http-client-apache-v2",
                "1.46.1",
                "jar");
        Artifact dependency4 = initArtifact(
                null,
                targetRepository,
                "com.google.http-client",
                "google-http-client-apache-v5",
                "1.46.1",
                "pom");
        Artifact dependency5 = initArtifact(
                null,
                targetRepository,
                "com.google.http-client",
                "google-http-client-apache-v5",
                "1.46.1",
                "jar");
        return List.of(dependency1, dependency2, dependency3, dependency4, dependency5);
    }

    private ProvenanceEntry createProvenanceEntry(String name, String urlRef, String urlSuffix) {
        return ProvenanceEntry.builder()
                .globalConfigUrlRef(urlRef)
                .provenanceEntryName(name)
                .resolverMethod(BuilderConfig.ResolverMethod.REPLACE.toName())
                .urlSuffix(urlSuffix)
                .build();

    }

    private List<ProvenanceEntry> createComponentVersions() {
        ProvenanceEntry cVersion1 = createProvenanceEntry(
                "https://github.com/project-ncl/environment-driver",
                "externalEnvironmentDriverUrl",
                "/version");
        ProvenanceEntry cVersion2 = createProvenanceEntry(
                "https://github.com/project-ncl/kafka-store",
                "externalKafkaStoreUrl",
                "/version");
        ProvenanceEntry cVersion3 = createProvenanceEntry(
                "https://github.com/project-ncl/pnc",
                "externalPncUrl",
                "/version");
        return List.of(cVersion1, cVersion2, cVersion3);
    }

    private List<ProvenanceEntry> createByProducts() {
        ProvenanceEntry byProduct1 = createProvenanceEntry(
                "buildLog",
                "externalPncUrl",
                "/builds/${buildId}/logs/build");
        ProvenanceEntry byProduct2 = createProvenanceEntry(
                "alignmentLog",
                "externalPncUrl",
                "/builds/${buildId}/logs/align");
        return List.of(byProduct1, byProduct2);
    }

    private static String randomHex(int byteLength) {
        byte[] bytes = new byte[byteLength];
        RANDOM.nextBytes(bytes);

        StringBuilder sb = new StringBuilder(byteLength * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

}
