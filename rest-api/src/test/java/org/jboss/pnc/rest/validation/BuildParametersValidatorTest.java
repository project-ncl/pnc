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
package org.jboss.pnc.rest.validation;

import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.rest.api.parameters.BuildParameters;
import org.jboss.pnc.rest.api.parameters.GroupBuildParameters;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import static org.jboss.pnc.api.enums.AlignmentPreference.PREFER_PERSISTENT;
import static org.jboss.pnc.api.enums.AlignmentPreference.PREFER_TEMPORARY;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@RunWith(Parameterized.class)
public class BuildParametersValidatorTest {

    private static Validator validator;

    private final boolean temporary;
    private final AlignmentPreference alignmentPreference;
    private final boolean isValid;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(
                new Object[][] { { true, PREFER_TEMPORARY, true }, { true, PREFER_PERSISTENT, true },
                        { false, PREFER_TEMPORARY, false }, { false, PREFER_PERSISTENT, false } });
    }

    public BuildParametersValidatorTest(boolean temporary, AlignmentPreference alignmentPreference, boolean isValid) {
        this.temporary = temporary;
        this.alignmentPreference = alignmentPreference;
        this.isValid = isValid;
    }

    @BeforeClass
    public static void setUp() {
        validator = Validation.byDefaultProvider()
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory()
                .getValidator();
    }

    @Test
    @Parameterized.Parameters
    public void testBuildParametersValidation() {
        BuildParameters buildParameters = new BuildParameters();
        buildParameters.setTemporaryBuild(temporary);
        buildParameters.setAlignmentPreference(alignmentPreference);

        Set<ConstraintViolation<BuildParameters>> violations = validator.validate(buildParameters);
        Assert.assertEquals(isValid, violations.isEmpty());
    }

    @Test
    @Parameterized.Parameters
    public void testGroupBuildParametersValidation() {
        GroupBuildParameters groupBuildParameters = new GroupBuildParameters();
        groupBuildParameters.setTemporaryBuild(temporary);
        groupBuildParameters.setAlignmentPreference(alignmentPreference);

        Set<ConstraintViolation<GroupBuildParameters>> violations = validator.validate(groupBuildParameters);
        Assert.assertEquals(isValid, violations.isEmpty());
    }
}
