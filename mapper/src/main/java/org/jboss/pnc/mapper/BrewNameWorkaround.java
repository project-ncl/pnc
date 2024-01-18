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
package org.jboss.pnc.mapper;

import org.jboss.pnc.constants.Attributes;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.model.BuildRecord;
import org.mapstruct.BeforeMapping;
import org.mapstruct.MappingTarget;

import java.util.HashMap;
import java.util.Map;

/**
 * Workaround for NCL-4889.
 * 
 * @author jbrazdil
 */
public class BrewNameWorkaround {

    @BeforeMapping
    @BuildHelpers
    @BuildHelpersNoBCRevision
    public static void mockBrewAttributes(BuildRecord build, @MappingTarget Build.Builder dtoBuilder) {
        Map<String, String> attributes = new HashMap<>(build.getAttributesMap());

        if (build.getExecutionRootName() != null) {
            attributes.putIfAbsent(Attributes.BUILD_BREW_NAME, build.getExecutionRootName());
        }
        if (build.getExecutionRootVersion() != null) {
            attributes.putIfAbsent(Attributes.BUILD_BREW_VERSION, build.getExecutionRootVersion());
        }
        dtoBuilder.attributes(attributes);
    }

    @BeforeMapping
    @BuildHelpers
    @BuildHelpersNoBCRevision
    public static void mockBrewAttributes(Build build, @MappingTarget BuildRecord.Builder entityBuilder) {
        Map<String, String> attributes = new HashMap<>(build.getAttributes());

        entityBuilder.executionRootName(attributes.remove(Attributes.BUILD_BREW_NAME));
        entityBuilder.executionRootVersion(attributes.remove(Attributes.BUILD_BREW_VERSION));
        entityBuilder.attributes(attributes);
    }
}
