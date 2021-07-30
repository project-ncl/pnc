/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2021 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.indyrepositorymanager.indyconfig;

import org.commonjava.cdi.util.weft.config.WeftConfig;

import java.util.Set;

public class PncWeftConfig implements WeftConfig {

    @Override
    public boolean isEnabled() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isEnabled(String poolName) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getThreads(String poolName) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getPriority(String poolName) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getMaxLoadFactor(String poolName) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getThreads(String poolName, Integer defaultThreads) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getPriority(String poolName, Integer defaultPriority) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getMaxLoadFactor(String poolName, Float defaultMax) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getDefaultThreads() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getDefaultPriority() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getDefaultMaxLoadFactor() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isLoadSensitive(String poolName, Boolean defaultLoadSensitive) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isDefaultLoadSensitive() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getNodePrefix() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<String> getKnownPools() {
        // TODO Auto-generated method stub
        return null;
    }

}
