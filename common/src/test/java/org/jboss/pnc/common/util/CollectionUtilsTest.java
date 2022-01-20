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
package org.jboss.pnc.common.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.pnc.common.util.CollectionUtils.hasCycle;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 10/7/16 Time: 10:50 AM
 */
public class CollectionUtilsTest {
    @Test
    public void shouldNotFindCycleInEmpty() {
        assertThat(hasCycle(new ArrayList<>(), this::extractChildren)).isFalse();
    }

    @Test
    public void shouldFindNoCycleInSeparateElements() {
        List<ListContainer> list = asList(
                new ListContainer(),
                new ListContainer(),
                new ListContainer(),
                new ListContainer());
        assertThat(hasCycle(list, this::extractChildren)).isFalse();
    }

    @Test
    public void shouldFindNoCycleInFlatList() {
        ListContainer a = new ListContainer();
        ListContainer b = new ListContainer(a);
        ListContainer c = new ListContainer(b);
        ListContainer d = new ListContainer(c);
        List<ListContainer> list = asList(a, b, c, d);
        assertThat(hasCycle(list, this::extractChildren)).isFalse();
    }

    @Test
    public void shouldFindNoCycleInTree() {
        ListContainer a = new ListContainer();
        ListContainer b = new ListContainer();
        ListContainer c = new ListContainer();
        ListContainer d = new ListContainer(a, b);
        ListContainer e = new ListContainer(d, c);
        List<ListContainer> list = asList(a, b, c, d, e);
        assertThat(hasCycle(list, this::extractChildren)).isFalse();
    }

    @Test
    public void shouldFindNoCycleInAcyclicGraph() {
        ListContainer a = new ListContainer();
        ListContainer b = new ListContainer();
        ListContainer c = new ListContainer(b);
        ListContainer d = new ListContainer(a, b);
        ListContainer e = new ListContainer(a, c, d);
        List<ListContainer> list = asList(a, b, c, d, e);
        assertThat(hasCycle(list, this::extractChildren)).isFalse();
    }

    @Test
    public void shouldFindCycleInTriangle() {
        ListContainer a = new ListContainer();
        ListContainer b = new ListContainer(a);
        ListContainer c = new ListContainer(b);
        a.children.add(c);
        assertThat(hasCycle(asList(a, b, c), this::extractChildren)).isTrue();
    }

    @Test
    public void shouldFindCycleInCyclicGraph() {
        ListContainer a = new ListContainer();
        ListContainer b = new ListContainer();
        ListContainer c = new ListContainer(b);
        ListContainer d = new ListContainer(a, b);
        ListContainer e = new ListContainer(a, d);
        a.children.add(c);
        c.children.add(e);
        List<ListContainer> list = asList(a, b, c, d, e);
        assertThat(hasCycle(list, this::extractChildren)).isTrue();
    }

    @Test
    public void shouldNotFailWithNullChildren() {
        ListContainer a = new ListContainer();
        ListContainer b = new ListContainer();
        ListContainer c = new ListContainer(b);
        ListContainer d = new ListContainer(a, b);
        ListContainer e = new ListContainer(a, d);
        a.children.add(c);
        c.children.add(e);
        b.children = null;
        List<ListContainer> list = asList(a, b, c, d, e);
        assertThat(hasCycle(list, this::extractChildren)).isTrue();
    }

    private List<ListContainer> extractChildren(ListContainer container) {
        return container.children;
    }

    private static class ListContainer {
        private List<ListContainer> children = new ArrayList<>();

        public ListContainer(ListContainer... children) {
            this.children.addAll(asList(children));
        }
    }

}