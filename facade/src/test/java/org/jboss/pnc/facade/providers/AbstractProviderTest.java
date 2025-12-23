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

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.json.moduleconfig.IndyRepoDriverModuleConfig;
import org.jboss.pnc.facade.rsql.RSQLProducer;
import org.jboss.pnc.mapper.AbstractArtifactMapperImpl;
import org.jboss.pnc.mapper.ArtifactRevisionMapperImpl;
import org.jboss.pnc.mapper.BuildBCRevisionFetcher;
import org.jboss.pnc.mapper.BuildConfigurationMapperImpl;
import org.jboss.pnc.mapper.BuildConfigurationRevisionMapperImpl;
import org.jboss.pnc.mapper.BuildMapperImpl;
import org.jboss.pnc.mapper.CollectionMerger;
import org.jboss.pnc.mapper.DeliverableAnalyzerOperationMapperImpl;
import org.jboss.pnc.mapper.EnvironmentMapperImpl;
import org.jboss.pnc.mapper.GroupBuildMapperImpl;
import org.jboss.pnc.mapper.GroupConfigurationMapperImpl;
import org.jboss.pnc.mapper.MapSetMapper;
import org.jboss.pnc.mapper.ProductMapperImpl;
import org.jboss.pnc.mapper.ProductMilestoneMapperImpl;
import org.jboss.pnc.mapper.ProductReleaseMapperImpl;
import org.jboss.pnc.mapper.ProductVersionMapperImpl;
import org.jboss.pnc.mapper.ProjectMapperImpl;
import org.jboss.pnc.mapper.RefToReferenceMapper;
import org.jboss.pnc.mapper.ResultMapperImpl;
import org.jboss.pnc.mapper.SCMRepositoryMapperImpl;
import org.jboss.pnc.mapper.TargetRepositoryMapperImpl;
import org.jboss.pnc.mapper.UserMapperImpl;
import org.jboss.pnc.mapper.api.ArtifactMapper;
import org.jboss.pnc.mapper.api.ArtifactRevisionMapper;
import org.jboss.pnc.mapper.api.BuildConfigurationMapper;
import org.jboss.pnc.mapper.api.BuildConfigurationRevisionMapper;
import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.mapper.api.DeliverableAnalyzerOperationMapper;
import org.jboss.pnc.mapper.api.EnvironmentMapper;
import org.jboss.pnc.mapper.api.GroupBuildMapper;
import org.jboss.pnc.mapper.api.GroupConfigurationMapper;
import org.jboss.pnc.mapper.api.ProductMapper;
import org.jboss.pnc.mapper.api.ProductMilestoneMapper;
import org.jboss.pnc.mapper.api.ProductReleaseMapper;
import org.jboss.pnc.mapper.api.ProductVersionMapper;
import org.jboss.pnc.mapper.api.ProjectMapper;
import org.jboss.pnc.mapper.api.ResultMapper;
import org.jboss.pnc.mapper.api.SCMRepositoryMapper;
import org.jboss.pnc.mapper.api.TargetRepositoryMapper;
import org.jboss.pnc.mapper.api.UserMapper;
import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;
import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 * @param <T> tested provider type
 */
public abstract class AbstractProviderTest<ID extends Serializable, T extends GenericEntity<ID>> {
    @Mock
    private Configuration configuration;

    @Mock
    protected PageInfoProducer pageInfoProducer;

    @Mock
    protected RSQLProducer rsqlPredicateProducer;

    @Mock
    protected BuildBCRevisionFetcher buildBCRevisionFetcher;

    @InjectMocks
    protected ArtifactMapper artifactMapper = spy(new AbstractArtifactMapperImpl());

    @InjectMocks
    protected ArtifactRevisionMapper artifactRevisionMapper = spy(new ArtifactRevisionMapperImpl());

    @InjectMocks
    protected BuildConfigurationRevisionMapper buildConfigurationRevisionMapper = spy(
            new BuildConfigurationRevisionMapperImpl());

    @InjectMocks
    protected BuildMapper buildMapper = spy(new BuildMapperImpl());

    @InjectMocks
    protected EnvironmentMapper environmentMapper = spy(new EnvironmentMapperImpl());

    @InjectMocks
    protected GroupBuildMapper groupBuildMapper = spy(new GroupBuildMapperImpl());

    @InjectMocks
    protected ProjectMapper projectMapper = spy(new ProjectMapperImpl());

    @InjectMocks
    protected ProductMapper productMapper = spy(new ProductMapperImpl());

    @InjectMocks
    protected SCMRepositoryMapper sCMRepositoryMapper = spy(new SCMRepositoryMapperImpl());

    @InjectMocks
    protected TargetRepositoryMapper targetRepositoryMapper = spy(new TargetRepositoryMapperImpl());

    @InjectMocks
    protected UserMapper userMapper = spy(new UserMapperImpl());

    @InjectMocks
    protected ProductVersionMapper productVersionMapper = spy(new ProductVersionMapperImpl());

    @InjectMocks
    protected GroupConfigurationMapper groupConfigurationMapper = spy(new GroupConfigurationMapperImpl());

    @InjectMocks
    protected BuildConfigurationMapper buildConfigurationMapper = spy(new BuildConfigurationMapperImpl());

    @InjectMocks
    protected ProductMilestoneMapper productMilestoneMapper = spy(new ProductMilestoneMapperImpl());

    @InjectMocks
    protected ProductReleaseMapper productReleaseMapper = spy(new ProductReleaseMapperImpl());

    @InjectMocks
    protected MapSetMapper mapSetMapper = spy(new MapSetMapper());

    @InjectMocks
    protected ResultMapper resultMapper = spy(new ResultMapperImpl());

    @InjectMocks
    protected DeliverableAnalyzerOperationMapper delAnalyzerOperationMapper = spy(
            new DeliverableAnalyzerOperationMapperImpl());

    @InjectMocks
    protected CollectionMerger collectionMerger = spy(new CollectionMerger());

    @Mock
    protected EntityManager em;

    @Spy
    @InjectMocks
    protected RefToReferenceMapper refMapper = spy(new RefToReferenceMapper());

    protected final List<T> repositoryList = new ArrayList<>();

    protected final Class<ID> idType;

    protected AbstractProviderTest(Class<ID> idType) {
        this.idType = idType;
    }

    protected abstract AbstractProvider provider();

    protected abstract Repository<T, ID> repository();

    @Before
    public void prepareMockInAbstract() throws ConfigurationParseException {
        GlobalModuleGroup globalConfig = new GlobalModuleGroup();
        globalConfig.setIndyUrl("http://url.com");
        globalConfig.setExternalIndyUrl("http://url.com");
        IndyRepoDriverModuleConfig indyRepoDriverModuleConfig = new IndyRepoDriverModuleConfig();
        when(pageInfoProducer.getPageInfo(anyInt(), anyInt())).thenAnswer(this::withPageInfo);
    }

    @Before
    public void prepareRepositoryMock() {
        when(em.getReference(any(), any())).thenAnswer(inv -> {
            Class<GenericEntity> type = inv.getArgument(0, Class.class);
            Serializable id = inv.getArgument(1, Serializable.class);
            GenericEntity mock = mock(type);
            when(mock.getId()).thenReturn(id);
            return mock;
        });
        when(repository().queryWithPredicates(any(), any(), any(Predicate[].class)))
                .thenAnswer(new ListAnswer<>(repositoryList));
        when(repository().count(any(Predicate[].class))).thenAnswer(inv -> repositoryList.size());
        when(repository().save(any())).thenAnswer(inv -> {
            T entity = inv.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(getNextId());
                repositoryList.add(entity);
                return entity;
            } else {
                for (int i = 0; i < repositoryList.size(); i++) {
                    if (repositoryList.get(i).getId().equals(entity.getId())) {
                        repositoryList.set(i, entity);
                        return entity;
                    }
                }
            }
            throw new IllegalArgumentException("Provided entity has ID but is not in the repository.");
        });
        when(repository().queryById(any(idType))).thenAnswer(inv -> {
            ID id = inv.getArgument(0);
            return repositoryList.stream().filter(a -> id.equals(a.getId())).findFirst().orElse(null);
        });
        doAnswer(inv -> {
            ID id = inv.getArgument(0);
            T object = repositoryList.stream().filter(a -> id.equals(a.getId())).findFirst().orElse(null);
            repositoryList.remove(object);
            return null;
        }).when(repository()).delete(any(idType));
    }

    protected abstract ID getNextId();

    protected void fillRepository(Collection<T> entities) {
        repositoryList.addAll(entities);
    }

    private PageInfo withPageInfo(InvocationOnMock inv) {
        int offset = inv.getArgument(0);
        int size = inv.getArgument(1);
        return new PageInfo() {
            @Override
            public int getPageSize() {
                return size;
            }

            @Override
            public int getPageOffset() {
                return offset;
            }
        };
    }

    protected static class ListAnswer<T> implements Answer<List<T>> {

        private final List<T> list;

        public ListAnswer(List<T> list) {
            this.list = list;
        }

        @Override
        public List<T> answer(InvocationOnMock invocation) throws Throwable {
            PageInfo pageInfo = invocation.getArgument(0);
            int first = pageInfo.getElementOffset();
            int last = first + pageInfo.getPageSize();
            if (last > list.size()) {
                last = list.size();
            }
            if (first > last) {
                first = last;
            }
            return list.subList(first, last);
        }
    }

}
