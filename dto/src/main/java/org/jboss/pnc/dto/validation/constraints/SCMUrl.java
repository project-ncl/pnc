/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.validation.constraints;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import org.jboss.pnc.dto.validation.validators.SCMUrlValidator;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 3/12/16 Time: 5:01 PM
 */
@Target({
        ElementType.METHOD,
        ElementType.FIELD,
        ElementType.ANNOTATION_TYPE,
        ElementType.CONSTRUCTOR,
        ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = SCMUrlValidator.class)
public @interface SCMUrl {
    String message() default "{org.jboss.pnc.rest.validation.InvalidScmUrl.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
