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

import org.commonjava.o11yphant.metrics.conf.ConsoleConfig;
import org.commonjava.o11yphant.metrics.conf.ELKConfig;
import org.commonjava.o11yphant.metrics.conf.GraphiteConfig;
import org.commonjava.o11yphant.metrics.conf.MetricsConfig;
import org.commonjava.o11yphant.metrics.conf.PrometheusConfig;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class IndyMetricsConfig implements MetricsConfig {

    private String nodePrefix;

    private boolean enabled;

    private String reporter;

    private ConsoleConfig consoleConfig;

    private GraphiteConfig graphiteConfig;

    private PrometheusConfig prometheusConfig;

    private ELKConfig elkConfig;

    private int meterRatio;

    public IndyMetricsConfig() {
        nodePrefix = "";
        enabled = false;
        reporter = null;
        consoleConfig = null;
        graphiteConfig = null;
        prometheusConfig = null;
        elkConfig = null;
        meterRatio = 0;
    }

    @Override
    public String getNodePrefix() {
        return nodePrefix;
    }

    @Override
    public boolean isEnabled() {
        // TODO Auto-generated method stub
        return enabled;
    }

    @Override
    public String getReporter() {
        return reporter;
    }

    @Override
    public ConsoleConfig getConsoleConfig() {
        return consoleConfig;
    }

    @Override
    public GraphiteConfig getGraphiteConfig() {
        return graphiteConfig;
    }

    @Override
    public PrometheusConfig getPrometheusConfig() {
        return prometheusConfig;
    }

    @Override
    public ELKConfig getELKConfig() {
        return elkConfig;
    }

    @Override
    public int getMeterRatio() {
        return meterRatio;
    }

}
