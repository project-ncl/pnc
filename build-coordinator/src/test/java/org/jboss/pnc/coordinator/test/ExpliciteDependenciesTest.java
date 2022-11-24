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
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.enums.RebuildMode;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeoutException;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class ExpliciteDependenciesTest extends AbstractDependentBuildTest {

    private BuildConfiguration a, b, c, d, e, f, g, h;
    private BuildConfiguration[] all;

    @Before
    public void setUp() throws TimeoutException, InterruptedException {
        // given
        d = config("d");
        c = config("c");
        b = config("b", d);
        a = config("a", b);
        all = new BuildConfiguration[] { d, c, b, a };
        insertNewBuildRecords(all);

        makeResult(b).dependOn(c);
        makeResult(a).dependOn(b, c);
    }

    @Test
    public void shouldBuildAOnModifiedB() throws TimeoutException, InterruptedException {
        // when
        insertNewBuildRecords(b);

        BuildOptions buildOptions = new BuildOptions();
        buildOptions.setRebuildMode(RebuildMode.EXPLICIT_DEPENDENCY_CHECK);
        build(a, buildOptions);
        // then
        expectBuilt(a);
    }

    @Test
    public void shouldNotBuildAOnModifiedC() throws TimeoutException, InterruptedException {
        // when
        insertNewBuildRecords(c);

        BuildOptions buildOptions = new BuildOptions();
        buildOptions.setRebuildMode(RebuildMode.EXPLICIT_DEPENDENCY_CHECK);
        build(a, buildOptions);
        // then
        expectBuilt();
    }

    @Test
    public void shouldBuildAOnModifiedCWhenImplicitDependencyCheck() throws TimeoutException, InterruptedException {
        // when
        insertNewBuildRecords(c);

        BuildOptions buildOptions = new BuildOptions();
        buildOptions.setBuildDependencies(false);
        buildOptions.setRebuildMode(RebuildMode.IMPLICIT_DEPENDENCY_CHECK);
        build(a, buildOptions);
        // then
        expectBuilt(a);
    }

    @Test
    public void shouldBuildABCOnForceAWithDependencies() throws TimeoutException, InterruptedException {
        // when
        insertNewBuildRecords(d, b, a);

        BuildOptions buildOptions = new BuildOptions();
        buildOptions.setBuildDependencies(true);
        buildOptions.setRebuildMode(RebuildMode.FORCE);
        build(a, buildOptions);
        // then
        expectBuilt(d, b, a);
    }
}
