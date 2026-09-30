/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mapper;

import java.util.HashMap;
import java.util.Map;

import org.jboss.pnc.constants.Attributes;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.model.BuildRecord;
import org.mapstruct.BeforeMapping;
import org.mapstruct.MappingTarget;

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
