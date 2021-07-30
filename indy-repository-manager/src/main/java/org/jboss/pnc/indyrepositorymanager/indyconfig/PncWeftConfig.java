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
