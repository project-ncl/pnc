/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.rest.provider;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.pnc.spi.datastore.repositories.CacheHandlerRepository;

import lombok.extern.slf4j.Slf4j;

@PermitAll
@Stateless
@Slf4j
public class CacheProvider {

    @Inject
    private CacheHandlerRepository cacheHandlerRepository; 
    
    @Deprecated
    public CacheProvider() {}

    @RolesAllowed("system-user")
    public String getStatistics() {
        log.debug("Get all statistics of second level cache");
        return cacheHandlerRepository.getCacheStatistics();
    }

    @RolesAllowed("system-user")
    public String getStatistics(Class entityClass) {
        log.debug("Get statistics of entity {} in second level cache", entityClass);
        return cacheHandlerRepository.getCacheStatistics(entityClass);
    }

    @RolesAllowed("system-user")
    public void clearAllCache() {
        log.debug("Evict all content from second level cache");
        cacheHandlerRepository.clearCache();
    }

    @RolesAllowed("system-user")
    public void clearCache(Class entityClass) {
        log.debug("Evict all content of entity {} from second level cache", entityClass);
        cacheHandlerRepository.clearCache(entityClass);
    }



}
