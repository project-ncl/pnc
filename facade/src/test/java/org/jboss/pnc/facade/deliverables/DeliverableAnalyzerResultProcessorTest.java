/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.facade.deliverables;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.pnc.api.deliverablesanalyzer.dto.Artifact;
import org.jboss.pnc.api.deliverablesanalyzer.dto.ArtifactType;
import org.jboss.pnc.api.deliverablesanalyzer.dto.Build;
import org.jboss.pnc.api.deliverablesanalyzer.dto.BuildSystemType;
import org.jboss.pnc.api.deliverablesanalyzer.dto.MavenArtifact;
import org.jboss.pnc.api.deliverablesanalyzer.dto.NPMArtifact;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.mapper.api.ArtifactMapper;
import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.TargetRepository;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;
import org.jboss.pnc.spi.datastore.repositories.TargetRepositoryRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.jboss.pnc.constants.ReposiotryIdentifier.DISTRIBUTION_ARCHIVE;
import static org.jboss.pnc.constants.ReposiotryIdentifier.INDY_MAVEN;
import static org.jboss.pnc.constants.ReposiotryIdentifier.INDY_NPM;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeliverableAnalyzerResultProcessorTest {

    @Mock
    private ProductMilestoneRepository milestoneRepository;
    @Mock
    private ArtifactRepository artifactRepository;
    @Mock
    private TargetRepositoryRepository targetRepositoryRepository;
    @Mock
    private ArtifactMapper artifactMapper;

    @InjectMocks
    private DeliverableAnalyzerResultProcessor processor;

    @Mock
    private ProductMilestone milestone;

    int id = 100;
    private List<TargetRepository> repositories = new ArrayList<>();
    private List<org.jboss.pnc.model.Artifact> artifacts = new ArrayList<>();

    @Before
    public void initMock() {
        when(artifactMapper.getIdMapper()).thenCallRealMethod();
        when(milestoneRepository.queryById(1)).thenReturn(milestone);
        repositories.clear();
        when(targetRepositoryRepository.save(any())).thenAnswer(new RepositorSave(repositories));
        when(targetRepositoryRepository.queryByIdentifierAndPath(any(), any())).thenAnswer(invocation -> {
            String identifier = invocation.getArgument(0);
            String path = invocation.getArgument(1);
            return repositories.stream()
                    .filter(tr -> identifier.equals(tr.getIdentifier()) && path.equals(tr.getIdentifier()))
                    .findAny()
                    .orElse(null);
        });
        when(artifactRepository.save(any())).thenAnswer(new RepositorSave(artifacts));
        when(artifactRepository.queryById(any())).thenAnswer(invocation -> {
            Integer id = invocation.getArgument(0);
            return artifacts.stream().filter(a -> id.equals(a.getId())).findAny().orElse(null);
        });
    }

    @Test
    public void testStore() {
        // with
        List<Build> builds = prepareBuilds();
        String distributionUrl = "https://example.com/distribution.zip";
        User user = User.Builder.newBuilder().id(42).username("TheUser").build();

        // when
        processor.processDeliverables(1, builds, distributionUrl, user);

        // verify that:
        // all artifacts were set as distributed
        verify(milestone, times(14)).addDistributedArtifact(any());
        // unknown artifacts were converted and set as distributed
        verify(milestone, times(2)).addDistributedArtifact(argThat(a -> {
            return a.getArtifactQuality().equals(ArtifactQuality.IMPORTED)
                    && a.getTargetRepository().getIdentifier().equals(DISTRIBUTION_ARCHIVE)
                    && a.getTargetRepository().getRepositoryType().equals(RepositoryType.DISTRIBUTION_ARCHIVE)
                    && a.getTargetRepository().getRepositoryPath().equals(distributionUrl);
        }));
        // brew unbuilt artifacts were converted and set as distributed
        verify(milestone, times(2)).addDistributedArtifact(argThat(a -> {
            return a.getArtifactQuality().equals(ArtifactQuality.IMPORTED)
                    && a.getTargetRepository().getIdentifier().equals(INDY_MAVEN)
                    && a.getTargetRepository().getRepositoryType().equals(RepositoryType.MAVEN);
        }));
        // brew built artifacts (in brew build "second-build-ever") were converted and set as distributed
        verify(milestone, times(2)).addDistributedArtifact(argThat(a -> {
            return a.getArtifactQuality().equals(ArtifactQuality.NEW)
                    && a.getTargetRepository().getIdentifier().equals(INDY_MAVEN)
                    && a.getTargetRepository().getRepositoryType().equals(RepositoryType.MAVEN)
                    && a.getTargetRepository().getRepositoryPath().contains("second-build-ever");
        }));
        // PNC artifacts were set as distributed
        verify(milestone, times(artifacts.size())).addDistributedArtifact(argThat(a -> artifacts.contains(a)));
        // user was set for milestone
        verify(milestone).setDistributedArtifactsImporter(user);
    }

    private List<Build> prepareBuilds() {
        List<Build> ret = new ArrayList<>();
        ret.add(prepareUnknownBuild());
        ret.add(preparePncBuild("1"));
        ret.add(preparePncBuild("2"));
        ret.add(prepareBrewBuild(1, "first-build-ever"));
        ret.add(prepareBrewBuild(2, "second-build-ever"));
        return ret;
    }

    private Build prepareUnknownBuild() {
        Set<Artifact> artifacts = new HashSet<>();
        artifacts.add(prepareUnknownArtifact("foo-bar-baz.xml"));
        artifacts.add(prepareUnknownArtifact("bazBarBoo.tar.gz"));
        return Build.builder().artifacts(artifacts).build();
    }

    private Build prepareBrewBuild(long id, String nvr) {
        Set<Artifact> artifacts = new HashSet<>();
        artifacts.add(prepareMavenArtifact("foo.bar", "baz" + id, "1.0.0.redhat-1", 123400 + id, true));
        artifacts.add(prepareMavenArtifact("foo.bar", "buzz" + id, "1.0.0.redhat-1", 567800 + id, true));
        artifacts.add(prepareMavenArtifact("bar.foo", "bizz" + id, "1.0.0", 951200 + id, false));
        return Build.builder()
                .buildSystemType(BuildSystemType.BREW)
                .brewId(id)
                .brewNVR(nvr)
                .artifacts(artifacts)
                .build();
    }

    private Build preparePncBuild(String id) {
        Set<Artifact> artifacts = new HashSet<>();
        artifacts.add(prepareMavenArtifact("foo.bar", "baz" + id, "1.0.0.redhat-1", "1234" + id));
        artifacts.add(prepareMavenArtifact("foo.bar", "buzz" + id, "1.0.0.redhat-1", "5678" + id));
        artifacts.add(prepareNPMArtifact("@redhat/foo-bar" + id, "1.0.0", "5678" + id));
        return Build.builder().buildSystemType(BuildSystemType.PNC).pncId(id).artifacts(artifacts).build();
    }

    private void prepareArtifact(Artifact.ArtifactBuilder builder, String filename) {
        builder.filename(filename)
                .md5("d7862759" + filename)
                .sha1("0ee53525" + filename)
                .sha256("8c26367d" + filename)
                .size(filename.length() * 100L);
    }

    private Artifact prepareUnknownArtifact(String filename) {
        Artifact.ArtifactBuilder builder = Artifact.builder().builtFromSource(false);
        prepareArtifact(builder, filename);
        return builder.build();
    }

    private MavenArtifact prepareMavenArtifact(
            String groupId,
            String artifactId,
            String version,
            Long brewId,
            boolean buildFromSource) {
        MavenArtifact.MavenArtifactBuilder builder = MavenArtifact.builder()
                .groupId(groupId)
                .artifactId(artifactId)
                .type("jar")
                .version(version)
                .artifactType(ArtifactType.MAVEN)
                .buildSystemType(BuildSystemType.BREW)
                .brewId(brewId)
                .builtFromSource(buildFromSource);
        prepareArtifact(builder, artifactId + "-" + version + " .jar");
        return builder.build();
    }

    private MavenArtifact prepareMavenArtifact(String groupId, String artifactId, String version, String pncId) {
        MavenArtifact.MavenArtifactBuilder builder = MavenArtifact.builder()
                .groupId(groupId)
                .artifactId(artifactId)
                .type("jar")
                .version(version)
                .artifactType(ArtifactType.MAVEN)
                .buildSystemType(BuildSystemType.PNC)
                .pncId(pncId)
                .builtFromSource(true);
        prepareArtifact(builder, artifactId + "-" + version + " .jar");
        MavenArtifact artifact = builder.build();
        savePNCArtifact(artifact, groupId + ":" + artifactId + ":jar:" + ":" + version);
        return artifact;
    }

    private NPMArtifact prepareNPMArtifact(String name, String version, String pncId) {
        NPMArtifact.NPMArtifactBuilder builder = NPMArtifact.builder()
                .name(name)
                .version(version)
                .artifactType(ArtifactType.NPM)
                .buildSystemType(BuildSystemType.PNC)
                .pncId(pncId)
                .builtFromSource(true);
        prepareArtifact(builder, name + "-" + version + " .zip");
        NPMArtifact artifact = builder.build();
        savePNCArtifact(artifact, name + ":" + version);
        return artifact;
    }

    private void savePNCArtifact(Artifact artifact, String identifier) {
        org.jboss.pnc.model.Artifact pncArtifact = org.jboss.pnc.model.Artifact.builder()
                .artifactQuality(ArtifactQuality.VERIFIED)
                .id(Integer.valueOf(artifact.getPncId()))
                .filename(artifact.getFilename())
                .identifier(identifier)
                .md5(artifact.getMd5())
                .sha1(artifact.getSha1())
                .sha256(artifact.getSha256())
                .size(artifact.getSize())
                .build();
        artifacts.add(pncArtifact);
    }

    private class RepositorSave<T extends GenericEntity<Integer>> implements Answer<T> {
        List<T> repository;
        int id = 100;

        public RepositorSave(List<T> repository) {
            this.repository = repository;
        }

        @Override
        public T answer(InvocationOnMock invocation) throws Throwable {
            T a = invocation.getArgument(0);
            a.setId(id++);
            repository.add(a);
            return a;
        }
    }
}
