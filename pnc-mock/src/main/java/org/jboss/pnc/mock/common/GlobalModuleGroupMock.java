/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mock.common;

import java.io.IOException;

import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.common.util.IoUtils;

public class GlobalModuleGroupMock {

    public static GlobalModuleGroup get() throws IOException {
        String configJson = IoUtils
                .readResource("globalGroupConfig.json", GlobalModuleGroupMock.class.getClassLoader());
        return JsonOutputConverterMapper.readValue(configJson, GlobalModuleGroup.class);
    }

}
