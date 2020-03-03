/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
import org.jboss.pnc.common.json.moduleconfig.IndyRepoDriverModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.facade.rsql.RSQLProducer;
import org.jboss.pnc.mapper.AbstractArtifactMapper;
import org.jboss.pnc.mapper.AbstractArtifactMapperImpl;
import org.jboss.pnc.mapper.BuildBCRevisionFetcher;
import org.jboss.pnc.mapper.BuildConfigurationMapperImpl;
import org.jboss.pnc.mapper.BuildConfigurationRevisionMapperImpl;
import org.jboss.pnc.mapper.BuildMapperImpl;
import org.jboss.pnc.mapper.EnvironmentMapperImpl;
import org.jboss.pnc.mapper.GroupBuildMapperImpl;
import org.jboss.pnc.mapper.GroupConfigurationMapperImpl;
import org.jboss.pnc.mapper.MapSetMapper;
import org.jboss.pnc.mapper.ProductMapperImpl;
import org.jboss.pnc.mapper.ProductMilestoneMapperImpl;
import org.jboss.pnc.mapper.ProductReleaseMapperImpl;
import org.jboss.pnc.mapper.ProductVersionMapperImpl;
import org.jboss.pnc.mapper.ProjectMapperImpl;
import org.jboss.pnc.mapper.ResultMapperImpl;
import org.jboss.pnc.mapper.SCMRepositoryMapperImpl;
import org.jboss.pnc.mapper.TargetRepositoryMapperImpl;
import org.jboss.pnc.mapper.UserMapperImpl;
import org.jboss.pnc.mapper.api.ArtifactMapper;
import org.jboss.pnc.mapper.api.BuildConfigurationMapper;
import org.jboss.pnc.mapper.api.BuildConfigurationRevisionMapper;
import org.jboss.pnc.mapper.api.BuildMapper;
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
import org.jboss.pnc.spi.datastore.repositories.api.Repository;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 * @param <T> tested provider type
 */
public abstract class AbstractProviderTest<T extends GenericEntity<Integer>> {
    @Mock
    private Configuration configuration;

    @Mock
    protected PageInfoProducer pageInfoProducer;

    @Mock
    protected RSQLProducer rsqlPredicateProducer;

    @Mock
    protected BuildBCRevisionFetcher buildBCRevisionFetcher;

    @Spy
    protected ArtifactMapper artifactMapper = new AbstractArtifactMapperImpl();

    @Spy
    protected BuildConfigurationRevisionMapper buildConfigurationRevisionMapper = new BuildConfigurationRevisionMapperImpl();

    @Spy
    protected BuildMapper buildMapper = new BuildMapperImpl();

    @Spy
    protected EnvironmentMapper environmentMapper = new EnvironmentMapperImpl();

    @Spy
    protected GroupBuildMapper groupBuildMapper = new GroupBuildMapperImpl();

    @Spy
    protected ProjectMapper projectMapper = new ProjectMapperImpl();

    @Spy
    protected ProductMapper productMapper = new ProductMapperImpl();

    @Spy
    protected SCMRepositoryMapper sCMRepositoryMapper = new SCMRepositoryMapperImpl();

    @Spy
    protected TargetRepositoryMapper targetRepositoryMapper = new TargetRepositoryMapperImpl();

    @Spy
    protected UserMapper userMapper = new UserMapperImpl();

    @Spy
    protected ProductVersionMapper productVersionMapper = new ProductVersionMapperImpl();

    @Spy
    protected GroupConfigurationMapper groupConfigurationMapper = new GroupConfigurationMapperImpl();

    @Spy
    protected BuildConfigurationMapper buildConfigurationMapper = new BuildConfigurationMapperImpl();

    @Spy
    protected ProductMilestoneMapper productMilestoneMapper = new ProductMilestoneMapperImpl();

    @Spy
    protected ProductReleaseMapper productReleaseMapper = new ProductReleaseMapperImpl();

    @Spy
    protected MapSetMapper mapSetMapper = new MapSetMapper();

    @Spy
    protected ResultMapper resultMapper = new ResultMapperImpl();

    protected int entityId = 1;

    protected final List<T> repositoryList = new ArrayList<>();

    @Before
    public void injectMappers() throws ReflectiveOperationException, IllegalArgumentException {
        injectMethod(
                "targetRepositoryMapper",
                artifactMapper,
                targetRepositoryMapper,
                AbstractArtifactMapperImpl.class);
        injectMethod("buildMapper", artifactMapper, buildMapper, AbstractArtifactMapperImpl.class);
        injectMethod("config", artifactMapper, configuration, AbstractArtifactMapper.class);

        injectMethod(
                "buildConfigurationRevisionMapper",
                buildMapper,
                buildConfigurationRevisionMapper,
                BuildMapperImpl.class);
        injectMethod("environmentMapper", buildMapper, environmentMapper, BuildMapperImpl.class);
        injectMethod("groupBuildMapper", buildMapper, groupBuildMapper, BuildMapperImpl.class);
        injectMethod("projectMapper", buildMapper, projectMapper, BuildMapperImpl.class);
        injectMethod("productMilestoneMapper", buildMapper, productMilestoneMapper, BuildMapperImpl.class);
        injectMethod("sCMRepositoryMapper", buildMapper, sCMRepositoryMapper, BuildMapperImpl.class);
        injectMethod("userMapper", buildMapper, userMapper, BuildMapperImpl.class);
        injectMethod("buildBCRevisionFetcher", buildMapper, buildBCRevisionFetcher, BuildMapperImpl.class);

        injectMethod(
                "environmentMapper",
                buildConfigurationMapper,
                environmentMapper,
                BuildConfigurationMapperImpl.class);
        injectMethod("mapSetMapper", buildConfigurationMapper, mapSetMapper, BuildConfigurationMapperImpl.class);
        injectMethod(
                "productVersionMapper",
                buildConfigurationMapper,
                productVersionMapper,
                BuildConfigurationMapperImpl.class);
        injectMethod("projectMapper", buildConfigurationMapper, projectMapper, BuildConfigurationMapperImpl.class);
        injectMethod(
                "sCMRepositoryMapper",
                buildConfigurationMapper,
                sCMRepositoryMapper,
                BuildConfigurationMapperImpl.class);

        injectMethod(
                "environmentMapper",
                buildConfigurationRevisionMapper,
                environmentMapper,
                BuildConfigurationRevisionMapperImpl.class);
        injectMethod(
                "projectMapper",
                buildConfigurationRevisionMapper,
                projectMapper,
                BuildConfigurationRevisionMapperImpl.class);
        injectMethod(
                "sCMRepositoryMapper",
                buildConfigurationRevisionMapper,
                sCMRepositoryMapper,
                BuildConfigurationRevisionMapperImpl.class);

        injectMethod(
                "productMilestoneMapper",
                productVersionMapper,
                productMilestoneMapper,
                ProductVersionMapperImpl.class);
        injectMethod("mapSetMapper", productVersionMapper, mapSetMapper, ProductVersionMapperImpl.class);
        injectMethod("productMapper", productVersionMapper, productMapper, ProductVersionMapperImpl.class);

        injectMethod(
                "productMilestoneMapper",
                productReleaseMapper,
                productMilestoneMapper,
                ProductReleaseMapperImpl.class);
        injectMethod(
                "productVersionMapper",
                productReleaseMapper,
                productVersionMapper,
                ProductReleaseMapperImpl.class);

        injectMethod("mapSetMapper", productMapper, mapSetMapper, ProductMapperImpl.class);

        injectMethod("mapSetMapper", projectMapper, mapSetMapper, ProjectMapperImpl.class);

        injectMethod(
                "productVersionMapper",
                productMilestoneMapper,
                productVersionMapper,
                ProductMilestoneMapperImpl.class);
        injectMethod(
                "productReleaseMapper",
                productMilestoneMapper,
                productReleaseMapper,
                ProductMilestoneMapperImpl.class);

        injectMethod("mapSetMapper", groupConfigurationMapper, mapSetMapper, GroupConfigurationMapperImpl.class);
        injectMethod(
                "productVersionMapper",
                groupConfigurationMapper,
                productVersionMapper,
                GroupConfigurationMapperImpl.class);

        injectMethod("userMapper", groupBuildMapper, userMapper, GroupBuildMapperImpl.class);
        injectMethod(
                "groupConfigurationMapper",
                groupBuildMapper,
                groupConfigurationMapper,
                GroupBuildMapperImpl.class);
        injectMethod("productVersionMapper", groupBuildMapper, productVersionMapper, GroupBuildMapperImpl.class);

        injectMethod("groupConfigurationMapper", mapSetMapper, groupConfigurationMapper, MapSetMapper.class);
        injectMethod("buildConfigurationMapper", mapSetMapper, buildConfigurationMapper, MapSetMapper.class);
        injectMethod("productVersionMapper", mapSetMapper, productVersionMapper, MapSetMapper.class);
        injectMethod("productMilestoneMapper", mapSetMapper, productMilestoneMapper, MapSetMapper.class);
        injectMethod("productReleaseMapper", mapSetMapper, productReleaseMapper, MapSetMapper.class);

        injectMethod("pageInfoProducer", provider(), pageInfoProducer, AbstractProvider.class);
        injectMethod("rsqlPredicateProducer", provider(), rsqlPredicateProducer, AbstractProvider.class);
    }

    protected abstract AbstractProvider provider();

    protected abstract Repository<T, Integer> repository();

    @Before
    public void prepareMockInAbstract() throws ConfigurationParseException {
        IndyRepoDriverModuleConfig indyRepoDriverModuleConfig = new IndyRepoDriverModuleConfig("http://url.com");
        indyRepoDriverModuleConfig.setExternalRepositoryMvnPath("http://url.com");
        indyRepoDriverModuleConfig.setExternalRepositoryNpmPath("http://url.com");
        indyRepoDriverModuleConfig.setInternalRepositoryMvnPath("http://url.com");
        indyRepoDriverModuleConfig.setInternalRepositoryNpmPath("http://url.com");
        when(configuration.getModuleConfig(new PncConfigProvider<>(IndyRepoDriverModuleConfig.class)))
                .thenReturn(indyRepoDriverModuleConfig);
        when(pageInfoProducer.getPageInfo(anyInt(), anyInt())).thenAnswer(this::withPageInfo);
    }

    @Before
    public void prepareRepositoryMock() {
        when(repository().queryWithPredicates(any(), any(), any())).thenAnswer(new ListAnswer(repositoryList));
        when(repository().count(any())).thenAnswer(inv -> repositoryList.size());
        when(repository().save(any())).thenAnswer(inv -> {
            T entity = inv.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(entityId++);
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
        when(repository().queryById(anyInt())).thenAnswer(inv -> {
            Integer id = inv.getArgument(0);
            return repositoryList.stream().filter(a -> id.equals(a.getId())).findFirst().orElse(null);
        });
        doAnswer(inv -> {
            Integer id = inv.getArgument(0);
            T object = repositoryList.stream().filter(a -> id.equals(a.getId())).findFirst().orElse(null);
            repositoryList.remove(object);
            return null;
        }).when(repository()).delete(anyInt());
    }

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

    private void injectMethod(String fieldName, Object to, Object what, Class clazz)
            throws NoSuchFieldException, IllegalAccessException {
        Field f = clazz.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(to, what);
    }

    protected static class ListAnswer<T> implements Answer<List<T>> {

        private final List<T> list;

        public ListAnswer(List<T> list) {
            this.list = list;
        }

        @Override
        public List<T> answer(InvocationOnMock invocation) throws Throwable {
            PageInfo pageInfo = invocation.getArgument(0);
            int first = pageInfo.getPageOffset();
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
