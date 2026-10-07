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

import org.jboss.pnc.dto.validation.validators.RefHasIdValidator;

/**
 * Validates that the refernced {@link org.jboss.pnc.dto.model.DTOEntity} has nun-null ID.
 * 
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = RefHasIdValidator.class)
public @interface RefHasId {

    String message() default "Reference must have a non-null ID.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * If the reference is optional (can be null). When the reference is not null, it must have non null ID.
     */
    boolean optional() default false;
}
