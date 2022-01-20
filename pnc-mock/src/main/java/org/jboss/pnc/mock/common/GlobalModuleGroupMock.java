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
package org.jboss.pnc.mock.common;

import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.common.util.IoUtils;

import java.io.IOException;

public class GlobalModuleGroupMock {

    public static GlobalModuleGroup get() throws IOException {
        String configJson = IoUtils
                .readResource("globalGroupConfig.json", GlobalModuleGroupMock.class.getClassLoader());
        return JsonOutputConverterMapper.readValue(configJson, GlobalModuleGroup.class);
    }

}
