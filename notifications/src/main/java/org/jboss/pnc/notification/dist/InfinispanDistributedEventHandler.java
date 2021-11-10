/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.notification.dist;

import java.util.Objects;
import java.util.Properties;

import io.apicurio.registry.utils.IoUtil;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.configuration.global.TransportConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryModified;
import org.infinispan.notifications.cachelistener.event.CacheEntryEvent;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;

@Listener(clustered = true, observation = Listener.Observation.POST)
public class InfinispanDistributedEventHandler extends AbstractDistributedEventHandler {
    private static final String DIST_EVENTS_CACHE = "dist-events-cache";

    private EmbeddedCacheManager manager;
    private Cache<String, String> eventsCache;

    private final SystemConfig config;

    public InfinispanDistributedEventHandler(SystemConfig config) {
        this.config = Objects.requireNonNull(config);
    }

    @Override
    public void sendEvent(Object event) {
        eventsCache.put(event.getClass().getName(), toMessage(event));
    }

    @Override
    public void start() {
        GlobalConfigurationBuilder gConf = GlobalConfigurationBuilder.defaultClusteredBuilder();

        String clusterName = Objects.requireNonNull(config.getInfinispanClusterName());
        TransportConfigurationBuilder transport = gConf.transport();
        transport.clusterName(clusterName);

        String tp = config.getInfinispanTransportProperties();
        if (tp != null) {
            Properties transportProperties = SystemConfig.readProperties(tp);
            if (transportProperties.size() > 0) {
                transport.withProperties(transportProperties);
            }
        }

        manager = new DefaultCacheManager(gConf.build());

        manager.defineConfiguration(
                DIST_EVENTS_CACHE,
                new ConfigurationBuilder().clustering().cacheMode(CacheMode.REPL_SYNC).build());
        eventsCache = manager.getCache(DIST_EVENTS_CACHE, true);
        eventsCache.addListener(this);
    }

    @CacheEntryCreated
    @CacheEntryModified
    public void handle(CacheEntryEvent<String, String> event) {
        sendMessage(event.getValue());
    }

    @Override
    public void close() {
        try {
            if (eventsCache != null) {
                eventsCache.removeListener(this);
            }
        } finally {
            IoUtil.close(manager);
        }
    }
}
