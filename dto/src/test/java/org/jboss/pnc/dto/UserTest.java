/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;
import org.junit.BeforeClass;
import org.junit.Test;

public class UserTest {

    private static Validator validator;

    @BeforeClass
    public static void setUp() {
        validator = Validation.byDefaultProvider()
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory()
                .getValidator();
    }

    @Test
    public void testUserNoHtml() {
        User user = new User(null, "Jack Ryan<a href=\"hahaha.com\"/>", Collections.emptySet());
        Set<ConstraintViolation<User>> constraintViolations = validator.validate(user, WhenCreatingNew.class);
        assertEquals(1, constraintViolations.size());

        user = new User("1234", "<blink>hello</blink>", Collections.emptySet());
        constraintViolations = validator.validate(user, WhenUpdating.class);
        assertEquals(1, constraintViolations.size());

        user = new User("<script>hi</script>", "<blink>hello</blink>", Collections.emptySet());
        constraintViolations = validator.validate(user, WhenUpdating.class);
        assertEquals(2, constraintViolations.size());

        // should not flag any issues
        user = new User("1234", "Feist", Collections.emptySet());
        constraintViolations = validator.validate(user, WhenUpdating.class);
        assertEquals(0, constraintViolations.size());

    }
}