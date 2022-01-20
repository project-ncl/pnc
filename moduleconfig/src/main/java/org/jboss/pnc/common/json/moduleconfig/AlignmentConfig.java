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
package org.jboss.pnc.common.json.moduleconfig;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jboss.pnc.common.json.AbstractModuleConfig;

import java.util.Map;

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
