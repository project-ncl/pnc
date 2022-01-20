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
package org.jboss.pnc.dto.validation.validators;

import org.jboss.pnc.dto.DTOEntity;
import org.jboss.pnc.dto.validation.constraints.RefHasId;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

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
