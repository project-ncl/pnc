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

package org.jboss.pnc.datastore.predicates.rsql;

import cz.jirutka.rsql.parser.RSQLParserException;
import org.assertj.core.api.Assertions;
import org.jboss.pnc.model.GenericEntity;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
public class JavaUtilPredicateTest {

    @Test
    public void shouldSelectProperInstance() throws Exception {
        //given
        List<TestClass> testedList = new ArrayList<>();
        String rsql = "field==test";

        testedList.add(new TestClass("test"));
        testedList.add(new TestClass("test1"));
        testedList.add(new TestClass(""));

        RSQLNodeTravellerPredicate rsqlNodeTravellerPredicate = new RSQLNodeTravellerPredicate(TestClass.class, rsql);

        //when
        Long numberOfInstances = testedList.stream().filter(rsqlNodeTravellerPredicate.getStreamPredicate()).count();

        //then
        Assertions.assertThat(numberOfInstances).isEqualTo(1);
    }

    @Test
    public void shouldSelectInstancesBasedOnTwoFields() throws Exception {
        //given
        List<TestClass> testedList = new ArrayList<>();
        String rsql = "field==a,field==b";

        testedList.add(new TestClass("a"));
        testedList.add(new TestClass("b"));
        testedList.add(new TestClass("c"));

        RSQLNodeTravellerPredicate rsqlNodeTravellerPredicate = new RSQLNodeTravellerPredicate(TestClass.class, rsql);

        //when
        Long numberOfInstances = testedList.stream().filter(rsqlNodeTravellerPredicate.getStreamPredicate()).count();

        //then
        Assertions.assertThat(numberOfInstances).isEqualTo(2);
    }

    @Test
    public void shouldGetValueBasedOnLikeExpression() throws Exception {
        //given
        List<TestClass> testedList = new ArrayList<>();
        String rsql = "field=like=te%";

        testedList.add(new TestClass("test"));
        testedList.add(new TestClass("test1"));
        testedList.add(new TestClass(""));

        RSQLNodeTravellerPredicate rsqlNodeTravellerPredicate = new RSQLNodeTravellerPredicate(TestClass.class, rsql);

        //when
        Long numberOfInstances = testedList.stream().filter(rsqlNodeTravellerPredicate.getStreamPredicate()).count();

        //then
        Assertions.assertThat(numberOfInstances).isEqualTo(2);
    }

    @Test
    public void shouldSelectInstanceIfNull() throws Exception {
        // given
        List<TestClass> testedList = new ArrayList<>();
        String rsql = "field=isnull=true";

        testedList.add(new TestClass(null));
        testedList.add(new TestClass("test1"));
        testedList.add(new TestClass("test2"));

        RSQLNodeTravellerPredicate rsqlNodeTravellerPredicate = new RSQLNodeTravellerPredicate(TestClass.class, rsql);

        // when
        Long numberOfInstances = testedList.stream().filter(rsqlNodeTravellerPredicate.getStreamPredicate()).count();

        // then
        Assertions.assertThat(numberOfInstances).isEqualTo(1);
    }

    @Test
    public void shouldSelectInstanceIfNotNull() throws Exception {
        // given
        List<TestClass> testedList = new ArrayList<>();
        String rsql = "field=isnull=false";

        testedList.add(new TestClass(null));
        testedList.add(new TestClass(null));
        testedList.add(new TestClass("test1"));

        RSQLNodeTravellerPredicate rsqlNodeTravellerPredicate = new RSQLNodeTravellerPredicate(TestClass.class, rsql);

        //when
        Long numberOfInstances = testedList.stream().filter(rsqlNodeTravellerPredicate.getStreamPredicate()).count();

        Assertions.assertThat(numberOfInstances).isEqualTo(1);
    }

    @Test
    public void shouldSelectItemsUsingInOperator() throws Exception {
        // given
        List<TestClass> testedList = new ArrayList<>();
        String rsql="id=in=(2,3)";

        TestClass testClass1 = new TestClass("Class 1", 1);
        TestClass testClass2 = new TestClass("Class 2", 2);
        TestClass testClass3 = new TestClass("Class 3", 3);

        testedList.add(testClass1);
        testedList.add(testClass2);
        testedList.add(testClass3);

        RSQLNodeTravellerPredicate rsqlNodeTravellerPredicate = new RSQLNodeTravellerPredicate(TestClass.class, rsql);

        //when
        Long numberOfInstances = testedList.stream().filter(rsqlNodeTravellerPredicate.getStreamPredicate()).count();

        //then
        Assertions.assertThat(numberOfInstances).isEqualTo(2);
    }

    @Test
    public void shouldSelectItemsUsingOutOperator() throws Exception {
        // given
        List<TestClass> testedList = new ArrayList<>();
        String rsql="id=out=(2,3)";

        TestClass testClass1 = new TestClass("Class 1", 1);
        TestClass testClass2 = new TestClass("Class 2", 2);
        TestClass testClass3 = new TestClass("Class 3", 3);

        testedList.add(testClass1);
        testedList.add(testClass2);
        testedList.add(testClass3);

        RSQLNodeTravellerPredicate rsqlNodeTravellerPredicate = new RSQLNodeTravellerPredicate(TestClass.class, rsql);

        //when
        Long numberOfInstances = testedList.stream().filter(rsqlNodeTravellerPredicate.getStreamPredicate()).count();

        //then
        Assertions.assertThat(numberOfInstances).isEqualTo(1);
    }

    @Test
    public void shouldCompareValue() throws Exception {
        //given
        List<TestClass> testedList = new ArrayList<>();
        String rsql = "id>2";

        testedList.add(new TestClass("test", 1));
        testedList.add(new TestClass("test1", 3));

        RSQLNodeTravellerPredicate rsqlNodeTravellerPredicate = new RSQLNodeTravellerPredicate(TestClass.class, rsql);

        //when
        Long numberOfInstances = testedList.stream().filter(rsqlNodeTravellerPredicate.getStreamPredicate()).count();

        //then
        Assertions.assertThat(numberOfInstances).isEqualTo(1);
    }

    @Test(expected = RSQLParserException.class)
    public void shouldThrowExceptionOnIncorrectSyntax() throws Exception {
        //given
        String rsql = "error";

        //when //then
        new RSQLNodeTravellerPredicate(TestClass.class, rsql);
    }

    public static class TestClass implements GenericEntity<Integer> {

        private String field;
        private Integer id;

        public TestClass(String field) {
            this.field = field;
        }

        public TestClass(String field, Integer id) {
            this.field = field;
            this.id = id;
        }

        @Override
        public Integer getId() {
            return id;
        }

        @Override
        public void setId(Integer id) {
            this.id = id;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }
    }

}
