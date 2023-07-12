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
package org.jboss.pnc.dto;

import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

import static org.junit.Assert.assertEquals;

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
        User user = new User("<a href=\"http://topsecret.com\" />", "Jack Ryan");
        Set<ConstraintViolation<User>> constraintViolations = validator.validate(user);
        assertEquals(1, constraintViolations.size());

        user = new User("1234", "<blink>hello</blink>");
        constraintViolations = validator.validate(user);
        assertEquals(1, constraintViolations.size());

        user = new User("<script>hi</script>", "<blink>hello</blink>");
        constraintViolations = validator.validate(user);
        assertEquals(2, constraintViolations.size());

        // should not flag any issues
        user = new User("1234", "Feist");
        constraintViolations = validator.validate(user);
        assertEquals(0, constraintViolations.size());

    }
}