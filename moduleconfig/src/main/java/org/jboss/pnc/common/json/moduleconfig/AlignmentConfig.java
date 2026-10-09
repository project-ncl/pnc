/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.json.moduleconfig;

import java.util.Map;

import org.jboss.pnc.common.json.AbstractModuleConfig;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AlignmentConfig extends AbstractModuleConfig {

    public static final String MODULE_NAME = "alignment-config";

    /**
     * Default alignment parameters concatenated into one string mapped to build type
     */
    private Map<String, String> alignmentParameters;

    public AlignmentConfig(@JsonProperty("alignmentParameters") Map<String, String> alignmentParameters) {
        this.alignmentParameters = alignmentParameters;
    }

    public Map<String, String> getAlignmentParameters() {
        return alignmentParameters;
    }
}
