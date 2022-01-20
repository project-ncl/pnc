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
package org.jboss.pnc.coordinator.test;

import org.jboss.pnc.model.BuildConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeoutException;

/**
 * Let A -> B, C denote that config A depends on configs B and C <br/>
 *
 *
 * <pre>
 *     A -> B, C
 *     B -> D, E, F
 *     C -> D. G
 *     G -> H
 * </pre>
 *
 *
 * <br>
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 11/30/16 Time: 3:57 PM
 */
public class TransitiveDependenciesTest extends AbstractDependentBuildTest {

    private BuildConfiguration a, b, c, d, e, f, g, h;
    private BuildConfiguration[] all;

    @Before
    public void setUp() throws TimeoutException, InterruptedException {
        // given
        h = config("h");
        g = config("g", h);
        f = config("f");
        e = config("e");
        d = config("d");
        c = config("c", d, g);
        b = config("b", d, e, f);
        a = config("a", b, c);
        all = new BuildConfiguration[] { h, g, f, e, d, c, b, a };
        insertNewBuildRecords(all);

        makeResult(g).dependOn(h);
        makeResult(c).dependOn(d, g);
        makeResult(b).dependOn(d, d, e, f);
        makeResult(a).dependOn(b, c);
    }

    @Test
    public void shouldBuildAOnModifiedA() throws TimeoutException, InterruptedException {
        // when
        modifyConfigurations(a);
        build(a);
        // then
        expectBuilt(a);
    }

    @Test
    public void shouldBuildABCDOnModifiedD() throws TimeoutException, InterruptedException {
        // when
        modifyConfigurations(d);
        build(a);

        // then
        expectBuilt(a, b, c, d);
    }

    @Test
    public void shouldBuildABEOnModifiedE() throws TimeoutException, InterruptedException {
        // when
        modifyConfigurations(e);
        build(a);
        // then
        expectBuilt(a, b, e);
    }

    @Test
    public void shouldBuildACGHOnModifiedH() throws TimeoutException, InterruptedException {
        // when
        modifyConfigurations(h);
        build(a);
        // then
        expectBuilt(a, c, g, h);
    }

    @Test
    public void shouldNotBuildWithoutModifications() throws TimeoutException, InterruptedException {
        // when
        build(a);
        // then
        expectBuilt();
    }

    @Test
    public void shouldBuildAllOnAllModified() throws TimeoutException, InterruptedException {
        // when
        modifyConfigurations(all);
        build(a);
        // then
        expectBuilt(all);
    }

    @Test
    public void shouldBuildAllOnModifiedDEFH() throws TimeoutException, InterruptedException {
        // when
        modifyConfigurations(d, e, f, h);
        build(a);
        // then
        expectBuilt(all);
    }
}
