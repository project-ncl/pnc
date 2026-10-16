/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.notification.dist;

import java.util.Objects;
import java.util.Properties;

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

import io.apicurio.registry.utils.IoUtil;

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
