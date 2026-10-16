/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.validation.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.jboss.pnc.dto.DTOEntity;
import org.jboss.pnc.dto.validation.constraints.RefHasId;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public class RefHasIdValidator implements ConstraintValidator<RefHasId, DTOEntity> {

    private boolean optional = false;

    @Override
    public void initialize(RefHasId constraintAnnotation) {
        optional = constraintAnnotation.optional();
    }

    @Override
    public boolean isValid(DTOEntity value, ConstraintValidatorContext context) {
        if (value == null) {
            return optional;
        } else {
            return value.getId() != null;
        }
    }

}
