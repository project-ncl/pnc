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
