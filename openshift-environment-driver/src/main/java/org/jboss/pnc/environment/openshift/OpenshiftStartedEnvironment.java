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
package org.jboss.pnc.environment.openshift;

import com.openshift.internal.restclient.model.Pod;
import com.openshift.internal.restclient.model.properties.ResourcePropertiesRegistry;
import com.openshift.restclient.ClientFactory;
import com.openshift.restclient.IClient;
import com.openshift.restclient.NoopSSLCertificateCallback;
import com.openshift.restclient.ResourceKind;
import com.openshift.restclient.authorization.TokenAuthorizationStrategy;
import org.jboss.dmr.ModelNode;
import org.jboss.pnc.common.json.moduleconfig.EnvironmentDriverModuleConfigBase;
import org.jboss.pnc.common.json.moduleconfig.OpenshiftEnvironmentDriverModuleConfig;
import org.jboss.pnc.common.monitor.PullingMonitor;
import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.jboss.pnc.spi.environment.StartedEnvironment;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.jboss.util.StringPropertyReplacer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class OpenshiftStartedEnvironment implements StartedEnvironment {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getName());

    private static final String OSE_API_VERSION = "1";
    private final IClient client;
    private Pod pod;
    private RepositorySession repositorySession;
    private OpenshiftEnvironmentDriverModuleConfig environmentConfiguration;
    private PullingMonitor pullingMonitor;

    public OpenshiftStartedEnvironment(
            OpenshiftEnvironmentDriverModuleConfig environmentConfiguration,
            PullingMonitor pullingMonitor,
            RepositorySession repositorySession) {
        this.environmentConfiguration = environmentConfiguration;
        this.pullingMonitor = pullingMonitor;
        this.repositorySession = repositorySession;

        client = new ClientFactory().create(environmentConfiguration.getRestEndpointUrl(), new NoopSSLCertificateCallback());
        client.setAuthorizationStrategy(new TokenAuthorizationStrategy(environmentConfiguration.getRestAuthToken()));

        String podConfiguration = replaceConfigurationVariables(Configurations.V1_PNC_BUILDER_POD.getContentAsString(), environmentConfiguration);

        ModelNode podConfigurationNode = ModelNode.fromJSONString(podConfiguration);
        pod = new Pod(podConfigurationNode, client, ResourcePropertiesRegistry.getInstance().get(OSE_API_VERSION, ResourceKind.POD));
        client.create(pod, environmentConfiguration.getPodNamespace());
    }

    @Override
    public void monitorInitialization(Consumer<RunningEnvironment> onComplete, Consumer<Exception> onError) {
        Runnable onEnvironmentInitComplete = () -> {
            logger.info("Pod successfully initiated. Name: " + pod.getName());

            Supplier<? extends RuntimeException> exceptionSupplier = () -> new RuntimeException("Cannot find container ports.");
            RunningEnvironment runningEnvironment = RunningEnvironment.createInstance(
                    pod.getName(),
                    pod.getContainerPorts().stream().findFirst().orElseThrow(exceptionSupplier).getContainerPort(),
                    pod.getHost() + environmentConfiguration.getBuildAgentBindPath(),
                    repositorySession,
                    Paths.get(environmentConfiguration.getWorkingDirectory()),
                    () -> destroyEnvironment()
            );

            onComplete.accept(runningEnvironment);
        };

        Consumer<Exception> onEnvironmentInitError = (e) -> {
            onError.accept(e);
        };

        pullingMonitor.monitor(onEnvironmentInitComplete, onEnvironmentInitError, () -> "Running".equals(pod.getStatus()));

        logger.info("Waiting to init services in a pod. Name: " + pod.getName());
    }

    @Override
    public String getId() {
        return pod.getName();
    }

    @Override
    public void destroyEnvironment() {
        client.delete(pod);
    }

    private String replaceConfigurationVariables(String podConfiguration, OpenshiftEnvironmentDriverModuleConfig environmentConfiguration) {
        Boolean proxyActive = !StringUtils.isEmpty(environmentConfiguration.getProxyServer())
                && !StringUtils.isEmpty(environmentConfiguration.getProxyPort());

        Properties properties = new Properties();
        properties.put("image", environmentConfiguration.getImageId());
        properties.put("containerPort", environmentConfiguration.getContainerPort());
        properties.put("firewallAllowedDestinations", environmentConfiguration.getFirewallAllowedDestinations());
        properties.put("isHttpActive", proxyActive.toString().toLowerCase());
        properties.put("proxyServer", environmentConfiguration.getProxyServer());
        properties.put("proxyPort", environmentConfiguration.getProxyPort());

        properties.put("AProxDependencyUrl", repositorySession.getConnectionInfo().getDependencyUrl());
        properties.put("AProxDeployUrl", repositorySession.getConnectionInfo().getDeployUrl());

        return StringPropertyReplacer.replaceProperties(podConfiguration, properties);
    }
}
