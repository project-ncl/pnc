/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.web;

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.common.json.moduleconfig.ScmModuleConfig;
import org.jboss.pnc.common.json.moduleconfig.UIModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class UiConfigRestBuilder {

    public static UiConfigRest build(Configuration configuration) throws ConfigurationParseException {
        GlobalModuleGroup globalConfig = configuration.getGlobalConfig();
        UIModuleConfig uiModuleConfig = configuration.getModuleConfig(new PncConfigProvider<>(UIModuleConfig.class));
        ScmModuleConfig scmModuleConfig = configuration.getModuleConfig(new PncConfigProvider<>(ScmModuleConfig.class));
        return new UiConfigRest(globalConfig, uiModuleConfig, scmModuleConfig.getInternalScmAuthority());
    }

    /**
     * Serializes the UI configuration to JSON, overriding the Bifrost and Kafka Store URLs served to the UI with the
     * UI-specific values from the global config when they are set. This lets the UI target different endpoints than the
     * backend services, which keep reading the original {@code externalBifrostUrl} / {@code externalKafkaStoreUrl}.
     */
    public static String toJson(UiConfigRest configRest) {
        GlobalModuleGroup globalConfig = configRest.getGlobalConfig();
        ObjectNode configNode = JsonOutputConverterMapper.getMapper().valueToTree(configRest);

        if (globalConfig.getUiBifrostUrl() != null) {
            configNode.put("externalBifrostUrl", globalConfig.getUiBifrostUrl());
        }
        if (globalConfig.getUiKafkaStoreUrl() != null) {
            configNode.put("externalKafkaStoreUrl", globalConfig.getUiKafkaStoreUrl());
        }

        // The ui* fields only drive the overrides above; they are not part of the UI config contract.
        configNode.remove("uiBifrostUrl");
        configNode.remove("uiKafkaStoreUrl");

        return configNode.toString();
    }
}
