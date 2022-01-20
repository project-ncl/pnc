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

import org.jboss.pnc.dto.validation.validators.RefHasIdValidator;

import javax.validation.Constraint;
import javax.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
