/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.json.moduleprovider;

import java.util.ArrayList;
import java.util.List;

import org.jboss.pnc.common.json.AbstractModuleConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;

/**
 * @author <a href="mailto:pslegr@redhat.com">pslegr</a> on Aug 21, 2015
 *
 * @param <T> module config
 */
public abstract class AbstractConfigProvider<T extends AbstractModuleConfig> implements ConfigProvider<T> {

    private List<ProviderNameType<T>> moduleConfigs = new ArrayList<>();
    private Class<T> ctype;

    public void registerProvider(ObjectMapper mapper) {
        for (ProviderNameType<T> providerNameType : moduleConfigs) {
            mapper.registerSubtypes(new NamedType(providerNameType.getType(), providerNameType.getTypeName()));
        }
    }

    public List<ProviderNameType<T>> getModuleConfigs() {
        return moduleConfigs;
    }

    public void addModuleConfig(ProviderNameType<T> providerNameType) {
        this.moduleConfigs.add(providerNameType);
    }

    public Class<T> getType() {
        return ctype;
    }

    protected void setType(Class<T> ctype) {
        this.ctype = ctype;
    }
}
