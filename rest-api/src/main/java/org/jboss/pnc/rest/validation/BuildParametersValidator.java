/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.jboss.pnc.rest.api.parameters.BuildParameters;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildParametersValidator implements ConstraintValidator<BuildParametersConstraint, BuildParameters> {
    @Override
    public void initialize(BuildParametersConstraint constraintAnnotation) {
    }

    /**
     * Allow AlignmentPreference for temporary build only.
     */
    @Override
    public boolean isValid(BuildParameters buildParameters, ConstraintValidatorContext constraintValidatorContext) {
        if (!buildParameters.isTemporaryBuild()) {
            return buildParameters.getAlignmentPreference() == null;
        } else {
            return true;
        }
    }
}
