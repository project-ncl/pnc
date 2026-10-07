/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.json.moduleprovider;

import java.util.List;

import org.jboss.pnc.common.json.AbstractModuleConfig;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author <a href="mailto:pslegr@redhat.com">pslegr</a> on Aug 21, 2015
 *
 * @param <T> module config
 */
public interface ConfigProvider<T extends AbstractModuleConfig> {

    void registerProvider(ObjectMapper mapper);

    List<ProviderNameType<T>> getModuleConfigs();

    void addModuleConfig(ProviderNameType<T> providerNameType);

    Class<T> getType();

}