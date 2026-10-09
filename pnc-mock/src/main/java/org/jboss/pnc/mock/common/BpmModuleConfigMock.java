/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mock.common;

import java.io.IOException;

import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.common.json.moduleconfig.BpmModuleConfig;
import org.jboss.pnc.common.util.IoUtils;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BpmModuleConfigMock {

    public static BpmModuleConfig get() throws IOException {
        String configJson = IoUtils.readResource("bpmModuleConfig.json", BpmModuleConfigMock.class.getClassLoader());
        return JsonOutputConverterMapper.readValue(configJson, BpmModuleConfig.class);
    }
}
