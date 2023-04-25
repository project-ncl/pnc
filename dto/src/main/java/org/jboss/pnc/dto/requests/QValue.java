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
package org.jboss.pnc.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jboss.pnc.api.dto.validation.ValidationResult;
import org.jboss.pnc.api.enums.Qualifier;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public class QValue {
    private final Qualifier qualifier;
    private final String[] value;

    public static QValue valueOf(String input) {
        if (!input.contains(":")) {
            throw new IllegalArgumentException("QValue " + input + " does not have ':' separator.");
        }
        String[] split = input.split(":", 2);
        System.out.println(Arrays.toString(split));
        Qualifier qualifier = Qualifier.valueOf(split[0].trim().replace('-', '_'));

        if (split[1].trim().isEmpty()) {
            throw new IllegalArgumentException("Qualifier " + qualifier + " does not have value.");
        }

        String[] parts = split[1].split(" ");
        ValidationResult result = qualifier.validate(parts);

        if (!result.valid) {
            throw new IllegalArgumentException(
                    "Validation for Qualifier " + qualifier + " failed: " + result.validationError);
        }

        return new QValue(qualifier, parts);
    }

    @Override
    public String toString() {
        return qualifier.name() + ':' + String.join(" ", value);
    }
}
