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
package org.jboss.pnc.dto.validation.constraints;

import org.jboss.pnc.dto.validation.validators.NoHtmlValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

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