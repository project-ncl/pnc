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
package org.jboss.pnc.facade.providers;

import org.assertj.core.api.Condition;
import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.facade.validation.DTOValidationException;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.TargetRepositoryRepository;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@RunWith(MockitoJUnitRunner.class)
public class ArtifactProviderTest extends AbstractIntIdProviderTest<org.jboss.pnc.model.Artifact> {

    @Mock
    private ArtifactRepository repository;

    @Mock
    private BuildRecordRepository buildRecordRepository;

    @Mock
    private TargetRepositoryRepository targetRepositoryRepository;

    @Spy
    @InjectMocks
    private ArtifactProviderImpl provider;

    private final List<org.jboss.pnc.model.Artifact> artifacts = new ArrayList<>();
    private final org.jboss.pnc.model.Artifact artifact1 = prepareArtifact("foo:bar:1.0.0", "abc1234a");

    @Override
    protected AbstractProvider provider() {
        return provider;
    }

    @Override
    protected Repository repository() {
        return repository;
    }

    public ArtifactProviderTest() {
        artifacts.add(artifact1);
        artifacts.add(prepareArtifact("foo:baz:1.0.0", "abc1234b"));
        artifacts.add(prepareArtifact("foo:bar:1.2.0", "abc1234c"));
        artifacts.add(prepareArtifact("foo:baz:1.2.0", "abc1234d"));
    }

    @Before
    public void prepareMocks() throws ReflectiveOperationException {
        when(rsqlPredicateProducer.getCriteriaPredicate(any(), anyString())).thenReturn(mock(Predicate.class));
        when(rsqlPredicateProducer.getSortInfo(any(), anyString())).thenReturn(mock(SortInfo.class));
    }

    @Test
    public void testStore() {
        final String identifier = "foo:bar:0.0.1";
        Artifact artifact = Artifact.builder().identifier(identifier).build();

        Artifact stored = provider.store(artifact);

        assertThat(stored.getIdentifier()).isEqualTo(identifier);
        assertThat(stored.getId()).isNotNull();
        Mockito.verify(repository).save(ArgumentMatchers.argThat(a -> identifier.equals(a.getIdentifier())));
    }

    @Test
    public void testStoreWithId() {
        final String identifier = "foo:bar:0.0.1";
        Artifact artifact = Artifact.builder().id("123").identifier(identifier).build();

        try {
            provider.store(artifact);
            fail("Validation exception expected");
        } catch (DTOValidationException ex) {
            // ok
        }
    }

    @Test
    public void testGetSpecific() {
        fillRepository(artifacts);

        Artifact specific = provider.getSpecific(Integer.toString(artifact1.getId()));

        assertThat(specific.getId()).isEqualTo(artifact1.getId().toString());
        assertThat(specific.getIdentifier()).isEqualTo(artifact1.getIdentifier());
        assertThat(specific.getSha256()).isEqualTo(artifact1.getSha256());
    }

    @Test
    public void testGetAll() {
        fillRepository(artifacts);

        Page<Artifact> all = provider.getAll(0, 10, null, null);

        assertThat(all.getContent()).hasSize(4)
                .haveExactly(
                        1,
                        new Condition<>(a -> artifact1.getIdentifier().equals(a.getIdentifier()), "Artifact present"));
    }

    @Test
    public void testUpdate() {
        fillRepository(artifacts);

        Artifact toUpdate = Artifact.builder()
                .id(artifact1.getId().toString())
                .artifactQuality(ArtifactQuality.BLACKLISTED) // Modified field
                .identifier(artifact1.getIdentifier())
                .md5(artifact1.getMd5())
                .sha1(artifact1.getSha1())
                .sha256(artifact1.getSha256())
                .build();

        assertThat(artifact1.getArtifactQuality()).isNotEqualTo(ArtifactQuality.BLACKLISTED); // assert that original is
                                                                                              // different

        Artifact updated = provider.update(Integer.toString(artifact1.getId()), toUpdate);

        assertThat(updated.getId()).isEqualTo(artifact1.getId().toString());
        assertThat(updated.getArtifactQuality()).isEqualTo(ArtifactQuality.BLACKLISTED); // modified
        assertThat(updated.getIdentifier()).isEqualTo(artifact1.getIdentifier());
        assertThat(updated.getSha256()).isEqualTo(artifact1.getSha256());
    }

    @Test
    public void testDelete() {
        fillRepository(artifacts);

        try {
            provider.delete(Integer.toString(artifact1.getId()));
            fail("Deleting artifact must be unsupported.");
        } catch (UnsupportedOperationException ex) {
            // ok
        }
    }

    private org.jboss.pnc.model.Artifact prepareArtifact(String identifier, String checksum) {
        return org.jboss.pnc.model.Artifact.builder()
                .id(entityId.getAndIncrement())
                .artifactQuality(ArtifactQuality.NEW)
                .identifier(identifier)
                .md5("md5-" + checksum)
                .sha1("sha1-" + checksum)
                .sha256("sha256-" + checksum)
                .build();
    }
}
