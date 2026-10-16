/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest.validation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Constraint(validatedBy = GroupBuildParametersValidator.class)
@Target({ TYPE })
@Retention(RUNTIME)
public @interface GroupBuildParametersConstraint {
    String message() default "Invalid Group build parameters.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
