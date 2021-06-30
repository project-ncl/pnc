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
package org.jboss.pnc.facade.rsql;

import org.hibernate.query.criteria.internal.path.SingularAttributePath;
import org.jboss.pnc.common.Numbers;
import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.dto.Project;
import org.jboss.pnc.enums.BuildType;
import org.jboss.pnc.facade.rsql.converter.Value;
import org.jboss.pnc.facade.rsql.converter.ValueConverter;
import org.jboss.pnc.facade.rsql.mapper.BuildRSQLMapper;
import org.jboss.pnc.facade.rsql.mapper.UniversalRSQLMapper;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.model.BuildEnvironment_;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecord_;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@RunWith(MockitoJUnitRunner.class)
public class RSQLPredicateProducerTest {

    @Mock
    UniversalRSQLMapper universalMapper;

    @InjectMocks
    RSQLProducerImpl producer = new RSQLProducerImpl();

    @Before
    public void setupMocks() {
        Class<?> foo = BuildRecord.class;
        when(universalMapper.toPath(ArgumentMatchers.same(BuildRecord.class), any(), any()))
                .then(callBuildRecordPath());
        ValueConverter valueConverter = mock(ValueConverter.class);
        when(universalMapper.getConverter()).thenReturn(valueConverter);
        when(valueConverter.convert(any(Value.class))).then(callBuildRecordConvert());
        when(valueConverter.convertComparable(any(Value.class))).then(callBuildRecordConvertComparable());
    }

    @Test
    public void testCriteriaPredicate() {
        Base32LongID id = new Base32LongID(4L);
        org.jboss.pnc.spi.datastore.repositories.api.Predicate<BuildRecord> criteriaPredicate = producer
                .getCriteriaPredicate(BuildRecord.class, "id==" + id.getId());

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Root<BuildRecord> root = mock(Root.class);
        SingularAttributePath<Base32LongID> idPath = mock(SingularAttributePath.class);

        when(root.get(BuildRecord_.id)).thenReturn(idPath);
        Mockito.doReturn(Base32LongID.class).when(idPath).getJavaType();

        SingularAttribute pathAttribute = mock(SingularAttribute.class);
        java.lang.reflect.Member javaMember = mock(java.lang.reflect.Member.class);
        Mockito.doReturn(BuildRecord.class).when(javaMember).getDeclaringClass();
        Mockito.doReturn(javaMember).when(pathAttribute).getJavaMember();
        Mockito.doReturn("id").when(pathAttribute).getName();
        Mockito.doReturn(pathAttribute).when(idPath).getAttribute();

        criteriaPredicate.apply(root, null, cb);

        Mockito.verify(cb).equal(idPath, id);
    }

    @Test
    public void testCriteriaPredicateEmbeded() {
        org.jboss.pnc.spi.datastore.repositories.api.Predicate<BuildRecord> criteriaPredicate = producer
                .getCriteriaPredicate(BuildRecord.class, "environment.name==fooEnv");

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Root<BuildRecord> root = mock(Root.class);
        Join<BuildRecord, BuildEnvironment> join = mock(Join.class);
        SingularAttributePath<String> namePath = mock(SingularAttributePath.class);

        when(root.join(BuildRecord_.buildEnvironment)).thenReturn(join);
        when(join.get(BuildEnvironment_.name)).thenReturn(namePath);
        Mockito.doReturn(String.class).when(namePath).getJavaType();

        SingularAttribute pathAttribute = mock(SingularAttribute.class);
        java.lang.reflect.Member javaMember = mock(java.lang.reflect.Member.class);
        Mockito.doReturn(BuildEnvironment.class).when(javaMember).getDeclaringClass();
        Mockito.doReturn(javaMember).when(pathAttribute).getJavaMember();
        Mockito.doReturn("name").when(pathAttribute).getName();
        Mockito.doReturn(pathAttribute).when(namePath).getAttribute();

        criteriaPredicate.apply(root, null, cb);

        Mockito.verify(cb).equal(namePath, "fooEnv");
    }

    @Test
    public void testCriteriaPredicateUnknownQuery() {
        org.jboss.pnc.spi.datastore.repositories.api.Predicate<BuildRecord> criteriaPredicate = producer
                .getCriteriaPredicate(BuildRecord.class, "fieldThatDoesNotExists==\"FooBar\"");

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Root<BuildRecord> root = mock(Root.class);

        try {
            criteriaPredicate.apply(root, null, cb);
            fail("Exception expected");
        } catch (RuntimeException ex) {
            // ok
        }
    }

    @Test
    public void testStreamPredicate() {
        Predicate<BuildConfiguration> streamPredicate = producer.getStreamPredicate("name==\"FooBar\"");

        BuildConfiguration fooBar = BuildConfiguration.builder().name("FooBar").build();
        BuildConfiguration fooBaz = BuildConfiguration.builder().name("FooBaz").build();

        List<BuildConfiguration> filtered = Arrays.asList(fooBar, fooBaz)
                .stream()
                .filter(streamPredicate)
                .collect(Collectors.toList());

        assertEquals(1, filtered.size());
        assertEquals("FooBar", filtered.get(0).getName());
    }

    @Test
    public void testStreamPredicateEmbeded() {
        Predicate<BuildConfiguration> streamPredicate = producer.getStreamPredicate("project.name==\"Bar Project\"");

        Project projBar = Project.builder().name("Bar Project").build();
        Project projBaz = Project.builder().name("Baz Project").build();
        BuildConfiguration fooBar = BuildConfiguration.builder().project(projBar).build();
        BuildConfiguration fooBaz = BuildConfiguration.builder().project(projBaz).build();

        List<BuildConfiguration> filtered = Arrays.asList(fooBar, fooBaz)
                .stream()
                .filter(streamPredicate)
                .collect(Collectors.toList());

        assertEquals(1, filtered.size());
        assertEquals("Bar Project", filtered.get(0).getProject().getName());
    }

    @Test
    public void testStreamPredicateUnknownQuery() {
        Predicate<BuildConfiguration> streamPredicate = producer
                .getStreamPredicate("fieldThatDoesNotExists==\"FooBar\"");

        BuildConfiguration fooBar = BuildConfiguration.builder().name("FooBar").build();
        BuildConfiguration fooBaz = BuildConfiguration.builder().name("FooBaz").build();

        try {
            List<BuildConfiguration> filtered = Arrays.asList(fooBar, fooBaz)
                    .stream()
                    .filter(streamPredicate)
                    .collect(Collectors.toList());
            fail("Exception expected");
        } catch (RuntimeException ex) {
            // ok
        }
    }

    @Test
    public void testComparator() {
        Comparator<BuildConfiguration> comparator = producer.getComparator("=desc=id");

        BuildConfiguration foo = BuildConfiguration.builder().id("3").name("FooBC").build();
        BuildConfiguration bar = BuildConfiguration.builder().id("5").name("BarBC").build();
        BuildConfiguration baz = BuildConfiguration.builder().id("7").name("BazBC").build();

        List<BuildConfiguration> sorted = Arrays.asList(foo, bar, baz)
                .stream()
                .sorted(comparator)
                .collect(Collectors.toList());

        assertEquals(3, sorted.size());
        assertEquals("BazBC", sorted.get(0).getName());
        assertEquals("BarBC", sorted.get(1).getName());
        assertEquals("FooBC", sorted.get(2).getName());
    }

    @Test
    public void testComparatorWithNullValue() {
        Comparator<BuildConfiguration> comparator = producer.getComparator("=desc=id");

        BuildConfiguration foo = BuildConfiguration.builder().id("3").name("FooBC").build();
        BuildConfiguration bar = BuildConfiguration.builder().id("5").name("BarBC").build();
        BuildConfiguration baz = BuildConfiguration.builder().id("7").name("BazBC").build();
        BuildConfiguration nul = BuildConfiguration.builder().id(null).name("NulBC").build();

        List<BuildConfiguration> sorted = Arrays.asList(foo, bar, baz, nul)
                .stream()
                .sorted(comparator)
                .collect(Collectors.toList());

        assertEquals(4, sorted.size());
        assertEquals("NulBC", sorted.get(0).getName());
        assertEquals("BazBC", sorted.get(1).getName());
        assertEquals("BarBC", sorted.get(2).getName());
        assertEquals("FooBC", sorted.get(3).getName());
    }

    @Test
    public void testComparatorUnknownQuery() {
        Comparator<BuildConfiguration> comparator = producer.getComparator("=desc=fieldThatDoesNotExists");

        BuildConfiguration foo = BuildConfiguration.builder().id("3").name("FooBC").build();
        BuildConfiguration bar = BuildConfiguration.builder().id("5").name("BarBC").build();
        BuildConfiguration baz = BuildConfiguration.builder().id("7").name("BazBC").build();

        try {
            List<BuildConfiguration> sorted = Arrays.asList(foo, bar, baz)
                    .stream()
                    .sorted(comparator)
                    .collect(Collectors.toList());
            fail("Exception expected");
        } catch (RuntimeException ex) {
            // ok
        }
    }

    @Test
    public void testPredicateWithTwoField() {
        Predicate<BuildConfiguration> streamPredicate = producer
                .getStreamPredicate("name==\"FooBar\";buildType==GRADLE");

        BuildConfiguration fooBarG = BuildConfiguration.builder().name("FooBar").buildType(BuildType.GRADLE).build();
        BuildConfiguration fooBarM = BuildConfiguration.builder().name("FooBar").buildType(BuildType.MVN).build();
        BuildConfiguration fooBazG = BuildConfiguration.builder().name("FooBaz").buildType(BuildType.GRADLE).build();

        List<BuildConfiguration> filtered = Arrays.asList(fooBarG, fooBarM, fooBazG)
                .stream()
                .filter(streamPredicate)
                .collect(Collectors.toList());

        assertEquals(1, filtered.size());
        assertEquals("FooBar", filtered.get(0).getName());
        assertEquals(BuildType.GRADLE, filtered.get(0).getBuildType());
    }

    @Test
    public void testPredicateLike() {
        Predicate<BuildConfiguration> streamPredicate = producer.getStreamPredicate("name=LIKE=\"%Bar\"");

        BuildConfiguration fooBarG = BuildConfiguration.builder().name("FooBar").buildType(BuildType.GRADLE).build();
        BuildConfiguration fooBarM = BuildConfiguration.builder().name("FooBar").buildType(BuildType.MVN).build();
        BuildConfiguration fooBazG = BuildConfiguration.builder().name("FooBaz").buildType(BuildType.GRADLE).build();

        List<BuildConfiguration> filtered = Arrays.asList(fooBarG, fooBarM, fooBazG)
                .stream()
                .filter(streamPredicate)
                .collect(Collectors.toList());

        assertEquals(2, filtered.size());
        assertEquals("FooBar", filtered.get(0).getName());
        assertEquals(BuildType.GRADLE, filtered.get(0).getBuildType());
        assertEquals("FooBar", filtered.get(1).getName());
        assertEquals(BuildType.MVN, filtered.get(1).getBuildType());
    }

    @Test
    public void testPredicateIsNull() {
        Predicate<BuildConfiguration> streamPredicate = producer.getStreamPredicate("name=ISNULL=true");

        BuildConfiguration fooBarG = BuildConfiguration.builder().name("FooBar").buildType(BuildType.GRADLE).build();
        BuildConfiguration nullName = BuildConfiguration.builder().name(null).buildType(BuildType.MVN).build();

        List<BuildConfiguration> filtered = Arrays.asList(fooBarG, nullName)
                .stream()
                .filter(streamPredicate)
                .collect(Collectors.toList());

        assertEquals(1, filtered.size());
        assertNull(filtered.get(0).getName());
        assertEquals(BuildType.MVN, filtered.get(0).getBuildType());
    }

    @Test
    public void testPredicateIsNotNull() {
        Predicate<BuildConfiguration> streamPredicate = producer.getStreamPredicate("name=ISNULL=false");

        BuildConfiguration fooBarG = BuildConfiguration.builder().name("FooBar").buildType(BuildType.GRADLE).build();
        BuildConfiguration nullName = BuildConfiguration.builder().name(null).buildType(BuildType.MVN).build();

        List<BuildConfiguration> filtered = Arrays.asList(fooBarG, nullName)
                .stream()
                .filter(streamPredicate)
                .collect(Collectors.toList());

        assertEquals(1, filtered.size());
        assertNotNull(filtered.get(0).getName());
        assertEquals(BuildType.GRADLE, filtered.get(0).getBuildType());
    }

    @Test
    public void testPredicateIn() {
        Predicate<BuildConfiguration> streamPredicate = producer.getStreamPredicate("id=in=(2,3)");

        BuildConfiguration fooBarG = BuildConfiguration.builder()
                .id("1")
                .name("FooBar")
                .buildType(BuildType.GRADLE)
                .build();
        BuildConfiguration fooBarM = BuildConfiguration.builder()
                .id("2")
                .name("FooBar")
                .buildType(BuildType.MVN)
                .build();
        BuildConfiguration fooBazG = BuildConfiguration.builder()
                .id("3")
                .name("FooBaz")
                .buildType(BuildType.GRADLE)
                .build();

        List<BuildConfiguration> filtered = Arrays.asList(fooBarG, fooBarM, fooBazG)
                .stream()
                .filter(streamPredicate)
                .collect(Collectors.toList());

        assertEquals(2, filtered.size());
        assertEquals("2", filtered.get(0).getId());
        assertEquals("FooBar", filtered.get(0).getName());
        assertEquals(BuildType.MVN, filtered.get(0).getBuildType());
        assertEquals("3", filtered.get(1).getId());
        assertEquals("FooBaz", filtered.get(1).getName());
        assertEquals(BuildType.GRADLE, filtered.get(1).getBuildType());
    }

    @Test
    public void testPredicateOut() {
        Predicate<BuildConfiguration> streamPredicate = producer.getStreamPredicate("id=out=(2,3)");

        BuildConfiguration fooBarG = BuildConfiguration.builder()
                .id("1")
                .name("FooBar")
                .buildType(BuildType.GRADLE)
                .build();
        BuildConfiguration fooBarM = BuildConfiguration.builder()
                .id("2")
                .name("FooBar")
                .buildType(BuildType.MVN)
                .build();
        BuildConfiguration fooBazG = BuildConfiguration.builder()
                .id("3")
                .name("FooBaz")
                .buildType(BuildType.GRADLE)
                .build();

        List<BuildConfiguration> filtered = Arrays.asList(fooBarG, fooBarM, fooBazG)
                .stream()
                .filter(streamPredicate)
                .collect(Collectors.toList());

        assertEquals(1, filtered.size());
        assertEquals("1", filtered.get(0).getId());
        assertEquals("FooBar", filtered.get(0).getName());
        assertEquals(BuildType.GRADLE, filtered.get(0).getBuildType());
    }

    @Test
    public void testPredicateCompareValue() {
        Predicate<BuildConfiguration> streamPredicate = producer.getStreamPredicate("id>1");

        BuildConfiguration fooBarG = BuildConfiguration.builder()
                .id("1")
                .name("FooBar")
                .buildType(BuildType.GRADLE)
                .build();
        BuildConfiguration fooBarM = BuildConfiguration.builder()
                .id("2")
                .name("FooBar")
                .buildType(BuildType.MVN)
                .build();
        BuildConfiguration fooBazG = BuildConfiguration.builder()
                .id("3")
                .name("FooBaz")
                .buildType(BuildType.GRADLE)
                .build();

        List<BuildConfiguration> filtered = Arrays.asList(fooBarG, fooBarM, fooBazG)
                .stream()
                .filter(streamPredicate)
                .collect(Collectors.toList());

        assertEquals(2, filtered.size());
        assertEquals("2", filtered.get(0).getId());
        assertEquals("FooBar", filtered.get(0).getName());
        assertEquals(BuildType.MVN, filtered.get(0).getBuildType());
        assertEquals("3", filtered.get(1).getId());
        assertEquals("FooBaz", filtered.get(1).getName());
        assertEquals(BuildType.GRADLE, filtered.get(1).getBuildType());
    }

    private Answer<Path<?>> callBuildRecordPath() {
        return invocation -> toPath(invocation.getArgument(1), invocation.getArgument(2));
    }

    private Path<?> toPath(From<?, BuildRecord> from, RSQLSelectorPath selector) {
        switch (selector.getElement()) {
            case "id":
                return from.get(BuildRecord_.id);
            case "environment":
                return toPathEnvironment(from.join(BuildRecord_.buildEnvironment), selector.next());
            default:
                throw new IllegalArgumentException("Unknown RSQL selector " + selector.getElement());
        }
    }

    private Path<?> toPathEnvironment(From<?, BuildEnvironment> from, RSQLSelectorPath selector) {
        switch (selector.getElement()) {
            case "name":
                return from.get(BuildEnvironment_.name);
            default:
                throw new IllegalArgumentException("Unknown RSQL selector " + selector.getElement());
        }
    }

    private Answer<?> callBuildRecordConvert() {
        return invocation -> {
            Value value = (Value) invocation.getArgument(0);
            return new BuildRSQLMapper().getValueConverter(value.getName()).convert(value);
        };
    }

    private Answer<?> callBuildRecordConvertComparable() {
        return invocation -> {
            Value value = (Value) invocation.getArgument(0);
            return new BuildRSQLMapper().getValueConverter(value.getName()).convertComparable(value);
        };
    }
}
