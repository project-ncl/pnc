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
import org.jboss.pnc.common.util.StringUtils;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 12/6/16 Time: 12:47 PM
 */
public class ScmModuleConfig extends AbstractModuleConfig {

    public static final String MODULE_NAME = "scm-config";

    private String internalScmAuthority;

    public ScmModuleConfig(@JsonProperty("internalScmAuthority") String internalScmAuthority) {
        super();
        this.internalScmAuthority = StringUtils.stripEndingSlash(internalScmAuthority);
    }

    public String getInternalScmAuthority() {
        return internalScmAuthority;
    }

    @Override
    public String toString() {
        return "ScmModuleConfig [internalScmAuthority=" + internalScmAuthority + "]";
    }
}
