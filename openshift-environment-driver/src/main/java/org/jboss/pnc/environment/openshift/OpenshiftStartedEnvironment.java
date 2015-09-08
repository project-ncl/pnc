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
import com.openshift.internal.restclient.model.Route;
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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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
    private Route route;
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
        String routePath = "pnc-ba-" + randString;

        runtimeProperties.put("pod-name", "pnc-ba-pod-" + randString);
        runtimeProperties.put("service-name", "pnc-ba-service-" + randString);
        runtimeProperties.put("route-name", "pnc-ba-route-" + randString);
        runtimeProperties.put("route-path", routePath);
        runtimeProperties.put("buildAgentContextPath", "/" + routePath);


        ModelNode podConfigurationNode = createModelNode(Configurations.V1_PNC_BUILDER_POD.getContentAsString(), runtimeProperties);
        pod = new Pod(podConfigurationNode, client, ResourcePropertiesRegistry.getInstance().get("v1", ResourceKind.POD, true));
        pod.setNamespace(environmentConfiguration.getPncNamespace());
        client.create(pod, environmentConfiguration.getPncNamespace()); //TODO non-blocking

        ModelNode serviceConfigurationNode = createModelNode(Configurations.V1_PNC_BUILDER_SERVICE.getContentAsString(), runtimeProperties);
        service = new Service(serviceConfigurationNode, client, ResourcePropertiesRegistry.getInstance().get("v1", ResourceKind.SERVICE, true));
        service.setNamespace(environmentConfiguration.getPncNamespace());
        client.create(service, environmentConfiguration.getPncNamespace()); //TODO non-blocking

        ModelNode routeConfigurationNode = createModelNode(Configurations.V1_PNC_BUILDER_ROUTE.getContentAsString(), runtimeProperties);
        route = new Route(routeConfigurationNode, client, ResourcePropertiesRegistry.getInstance().get("v1", ResourceKind.ROUTE, true));
        route.setNamespace(environmentConfiguration.getPncNamespace());
        client.create(route, environmentConfiguration.getPncNamespace()); //TODO non-blocking
    }

    private ModelNode createModelNode(String resourceDefinition, Map<String, String> runtimeProperties) {
        String definition = replaceConfigurationVariables(resourceDefinition, runtimeProperties);
        logger.info("Node definition: " + definition);
        return ModelNode.fromJSONString(definition);
    }

    @Override
    public void monitorInitialization(Consumer<RunningEnvironment> onComplete, Consumer<Exception> onError) {

        pullingMonitor.monitor(onEnvironmentInitComplete(onComplete, Selector.POD), onError, () -> isPodRunning());
        pullingMonitor.monitor(onEnvironmentInitComplete(onComplete, Selector.SERVICE), onError, () -> isServiceRunning());
        pullingMonitor.monitor(onEnvironmentInitComplete(onComplete, Selector.ROUTE), onError, () -> isRouteRunning());

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
                    //TODO use service for internal communication
                    getEndpointUrl(), //TODO configurable port and protocol
                    repositorySession,
                    Paths.get(environmentConfiguration.getWorkingDirectory()),
                    this::destroyEnvironment
            );

            onComplete.accept(runningEnvironment);
        };
    }

    private String getEndpointUrl() {
        return "http://" + route.getHost() + route.getPath() + environmentConfiguration.getBuildAgentBindPath();
    }

    private boolean isPodRunning() {
        pod = client.get(this.pod.getKind(), this.pod.getName(), environmentConfiguration.getPncNamespace());
        return "Running".equals(pod.getStatus());
    }

    private boolean isServiceRunning() {
        service = client.get(this.service.getKind(), this.service.getName(), environmentConfiguration.getPncNamespace());
        return service.getPods().size() > 0;
    }

    private boolean isRouteRunning() {
        try {
            if (connectToPingUrl(new URL(getEndpointUrl()))) {
                route = client.get(this.route.getKind(), this.route.getName(), environmentConfiguration.getPncNamespace());
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            logger.error("Cannot open URL " + getEndpointUrl(), e);
            return false;
        }
    }

    @Override
    public String getId() {
        return pod.getName();
    }

    @Override
    public void destroyEnvironment() {
        client.delete(route);
        client.delete(service);
        client.delete(pod);
    }

    private String replaceConfigurationVariables(String podConfiguration, Map runtimeProperties) {
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
        POD, SERVICE, ROUTE;
    }

    private boolean connectToPingUrl(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(250);
        connection.setRequestMethod("GET");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.connect();

        int responseCode = connection.getResponseCode();
        connection.disconnect();

        logger.debug("Got {} from {}.", responseCode, url);
        return responseCode == 200;
    }
}
