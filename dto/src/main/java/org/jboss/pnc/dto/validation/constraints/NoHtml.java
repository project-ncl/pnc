/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.validation.constraints;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import org.jboss.pnc.dto.validation.validators.NoHtmlValidator;

/**
 * Annotation to add to a method or a field to validate that the String doesn't contain HTML tags. This is useful to
 * prevent XSS attacks.
 *
 * It should work out of the box with Hibernate Validator
 *
 * Copied from: https://stackoverflow.com/a/68888601/2907906
 */
@Documented
@Constraint(validatedBy = NoHtmlValidator.class)
@Target({ METHOD, FIELD })
@Retention(RUNTIME)
public @interface NoHtml {
    String message() default "Unsafe html content";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}