/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.termdbuilddriver;

import org.jboss.pnc.model.BuildType;
import org.junit.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;

public class BuildTypeTest {

    @Test
    public void shouldBuildOnlyJava() throws Exception {
        //given
        BuildType allowedBuildType = BuildType.JAVA;
        TermdBuildDriver termdBuildDriver = new TermdBuildDriver();

        //when
        boolean canBuild = termdBuildDriver.canBuild(allowedBuildType);

        //then
        assertThat(canBuild).isEqualTo(true);
    }

    @Test
    public void shouldNotBuildOtherTypes() throws Exception {
        //given
        EnumSet<BuildType> notAllowedBuildTypes = EnumSet.complementOf(EnumSet.of(BuildType.JAVA));
        TermdBuildDriver termdBuildDriver = new TermdBuildDriver();

        //when
        notAllowedBuildTypes.stream().forEach(notAllowedBuildType -> {
            boolean canBuild = termdBuildDriver.canBuild(notAllowedBuildType);

            //then
            assertThat(canBuild).isEqualTo(false);
        });
    }

}