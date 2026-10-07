/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.integration.mock;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.GlobalModuleGroup;

@Alternative
@ApplicationScoped
public class ConfigurationPersistentBuildingDisabledMock extends Configuration {

    public ConfigurationPersistentBuildingDisabledMock() throws ConfigurationParseException {
        super();
    }

    @Override
    public GlobalModuleGroup getGlobalConfig() throws ConfigurationParseException {
        GlobalModuleGroup globalModuleGroup = new GlobalModuleGroup();
        globalModuleGroup.setPersistentBuildingAllowed(false);
        return globalModuleGroup;
    }
}
