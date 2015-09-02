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
import com.openshift.internal.restclient.model.Service;
import com.openshift.internal.restclient.model.properties.ResourcePropertiesRegistry;
import com.openshift.restclient.ClientFactory;
import com.openshift.restclient.IClient;
import com.openshift.restclient.NoopSSLCertificateCallback;
import com.openshift.restclient.ResourceKind;
import com.openshift.restclient.authorization.TokenAuthorizationStrategy;
import org.jboss.dmr.ModelNode;
import org.jboss.pnc.common.json.moduleconfig.OpenshiftEnvironmentDriverModuleConfig;
import org.jboss.pnc.common.monitor.PullingMonitor;
import org.jboss.pnc.common.util.RandomUtils;
import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.jboss.pnc.spi.environment.StartedEnvironment;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.jboss.util.StringPropertyReplacer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class OpenshiftStartedEnvironment implements StartedEnvironment {

    private static final Logger logger = LoggerFactory.getLogger(OpenshiftStartedEnvironment.class);

    private static final String OSE_API_VERSION = "1";
    private final IClient client;
    private final RepositorySession repositorySession;
    private final OpenshiftEnvironmentDriverModuleConfig environmentConfiguration;
    private final PullingMonitor pullingMonitor;
    private Pod pod;
    private Service service;
    private final Set<Selector> initialized = new HashSet<>();


    public OpenshiftStartedEnvironment(
            OpenshiftEnvironmentDriverModuleConfig environmentConfiguration,
            PullingMonitor pullingMonitor,
            RepositorySession repositorySession) {
        logger.info("Creating new build environment using image id: " + environmentConfiguration.getImageId());
        this.environmentConfiguration = environmentConfiguration;
        this.pullingMonitor = pullingMonitor;
        this.repositorySession = repositorySession;

        client = new ClientFactory().create(environmentConfiguration.getRestEndpointUrl(), new NoopSSLCertificateCallback());
        client.setAuthorizationStrategy(new TokenAuthorizationStrategy(environmentConfiguration.getRestAuthToken()));

        Map<String, String> runtimeProperties = new HashMap<>();
        String randString = RandomUtils.randString(4);//TODO increment length, not the 24 char limit
        runtimeProperties.put("pod-name", "pnc-ba-pod-" + randString);
        runtimeProperties.put("service-name", "pnc-ba-service-" + randString);

        String podConfiguration = replaceConfigurationVariables(Configurations.V1_PNC_BUILDER_POD.getContentAsString(), environmentConfiguration, runtimeProperties);
        logger.info("Using Pod definition: " + podConfiguration);
        ModelNode podConfigurationNode = ModelNode.fromJSONString(podConfiguration);
        pod = new Pod(podConfigurationNode, client, ResourcePropertiesRegistry.getInstance().get("v1", ResourceKind.POD, true));
        pod.setNamespace(environmentConfiguration.getPncNamespace());
        client.create(pod, environmentConfiguration.getPncNamespace()); //TODO non-blocking

        String serviceConfiguration = replaceConfigurationVariables(Configurations.V1_PNC_BUILDER_SERVICE.getContentAsString(), environmentConfiguration, runtimeProperties);
        logger.info("Using Service definition: " + serviceConfiguration);
        ModelNode serviceConfigurationNode = ModelNode.fromJSONString(serviceConfiguration);
        service = new Service(serviceConfigurationNode, client, ResourcePropertiesRegistry.getInstance().get("v1", ResourceKind.SERVICE, true));
        service.setNamespace(environmentConfiguration.getPncNamespace());
        client.create(service, environmentConfiguration.getPncNamespace()); //TODO non-blocking
    }

    @Override
    public void monitorInitialization(Consumer<RunningEnvironment> onComplete, Consumer<Exception> onError) {

        pullingMonitor.monitor(onEnvironmentInitComplete(onComplete, Selector.POD), onError, () -> isPodRunning());
        pullingMonitor.monitor(onEnvironmentInitComplete(onComplete, Selector.SERVICE), onError, () -> isServiceRunning());

        logger.info("Waiting to start a pod [{}] and service [{}].", pod.getName(), service.getName());
    }

    private Runnable onEnvironmentInitComplete(Consumer<RunningEnvironment> onComplete, Selector selector) {
        return () -> {
            synchronized (this) {
                initialized.add(selector);
                if (!initialized.containsAll(Arrays.asList(Selector.values()))) {
                    return;
                }
            }

            logger.info("Pod [{}] and service [{}] successfully initialized.", pod.getName(), service.getName());

            Supplier<? extends RuntimeException> exceptionSupplier = () -> new RuntimeException("Cannot find container ports.");
            RunningEnvironment runningEnvironment = RunningEnvironment.createInstance(
                    pod.getName(),
                    pod.getContainerPorts().stream().findFirst().orElseThrow(exceptionSupplier).getContainerPort(),
                    "http://" + service.getPortalIP() + environmentConfiguration.getBuildAgentBindPath(),
                    repositorySession,
                    Paths.get(environmentConfiguration.getWorkingDirectory()),
                    this::destroyEnvironment
            );

            onComplete.accept(runningEnvironment);
        };
    }

    private boolean isPodRunning() {
        pod = client.get(this.pod.getKind(), this.pod.getName(), environmentConfiguration.getPncNamespace());
        return "Running".equals(pod.getStatus());
    }

    private boolean isServiceRunning() {
        service = client.get(this.service.getKind(), this.service.getName(), environmentConfiguration.getPncNamespace());
        return service.getPods().size() > 0;
    }

    @Override
    public String getId() {
        return pod.getName();
    }

    @Override
    public void destroyEnvironment() {
        client.delete(service);
        client.delete(pod);
    }

    private String replaceConfigurationVariables(String podConfiguration, OpenshiftEnvironmentDriverModuleConfig environmentConfiguration, Map runtimeProperties) {
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

        properties.putAll(runtimeProperties);

        return StringPropertyReplacer.replaceProperties(podConfiguration, properties);
    }

    private enum Selector {
        POD, SERVICE;
    }
}
