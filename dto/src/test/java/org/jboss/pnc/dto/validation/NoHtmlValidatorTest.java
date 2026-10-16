/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.jboss.pnc.dto.validation.validators.NoHtmlValidator;
import org.junit.Test;

public class NoHtmlValidatorTest {
    @Test
    public void testValidator() {
        NoHtmlValidator validator = new NoHtmlValidator();
        assertThat(validator.isValid("hello", null)).isTrue();
        assertThat(validator.isValid("<alert>hello</alert>", null)).isFalse();
        assertThat(validator.isValid("<script>hello</script>", null)).isFalse();
        assertThat(validator.isValid("<?php hello>", null)).isFalse();
        assertThat(validator.isValid("hello heya <div>asdf</div>", null)).isFalse();
        assertThat(validator.isValid("hello heya <a href=\"heloo\" />", null)).isFalse();

        // null has no html!
        assertThat(validator.isValid(null, null)).isTrue();
        assertThat(validator.isValid("", null)).isTrue();
    }

    @Test
    public void testValidatorAnnotation() {
        Validator validator = Validation.byDefaultProvider()
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory()
                .getValidator();

        NoHtmlDTO dto = new NoHtmlDTO();
        dto.test = "<div>hello</div>";
        Set<ConstraintViolation<NoHtmlDTO>> constraintViolations = validator.validate(dto);
        assertThat(constraintViolations.size()).isEqualTo(1);

        dto.test = "whats up whats going on";
        constraintViolations = validator.validate(dto);
        assertThat(constraintViolations.size()).isEqualTo(0);
    }
}