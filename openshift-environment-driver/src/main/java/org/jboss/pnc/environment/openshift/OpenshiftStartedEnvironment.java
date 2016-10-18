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
import org.apache.commons.lang.RandomStringUtils;
import org.jboss.dmr.ModelNode;
import org.jboss.pnc.common.json.moduleconfig.OpenshiftEnvironmentDriverModuleConfig;
import org.jboss.pnc.environment.monitor.PullingMonitor;
import org.jboss.pnc.common.util.RandomUtils;
import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.spi.builddriver.DebugData;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.jboss.pnc.spi.environment.StartedEnvironment;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.jboss.util.StringPropertyReplacer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class OpenshiftStartedEnvironment implements StartedEnvironment {

    private static final Logger logger = LoggerFactory.getLogger(OpenshiftStartedEnvironment.class);
    private static final String SSH_SERVICE_PORT_NAME = "2222-ssh";
    private static final String POD_USERNAME = "worker";
    private static final String POD_USER_PASSWD = "workerUserPassword";
    private boolean serviceCreated = false;
    private boolean podCreated = false;
    private boolean routeCreated = false;

    private static final String OSE_API_VERSION = "v1";
    private final IClient client;
    private final RepositorySession repositorySession;
    private final OpenshiftEnvironmentDriverModuleConfig environmentConfiguration;
    private final PullingMonitor pullingMonitor;
    private Pod pod;
    private Service service;
    private Route route;
    private Service sshService;
    private final DebugData debugData;
    private final Set<Selector> initialized = new HashSet<>();
    private final Map<String, String> runtimeProperties;

    private final String buildAgentContextPath;

    private final boolean createRoute;


    public OpenshiftStartedEnvironment(
            ExecutorService executor,
            OpenshiftEnvironmentDriverModuleConfig environmentConfiguration,
            PullingMonitor pullingMonitor,
            RepositorySession repositorySession,
            DebugData debugData) {

        logger.info("Creating new build environment using image id: " + environmentConfiguration.getImageId());

        this.environmentConfiguration = environmentConfiguration;
        this.pullingMonitor = pullingMonitor;
        this.repositorySession = repositorySession;
        this.debugData = debugData;

        createRoute = environmentConfiguration.getExposeBuildAgentOnPublicUrl();

        client = new ClientFactory().create(environmentConfiguration.getRestEndpointUrl(), new NoopSSLCertificateCallback());
        client.setAuthorizationStrategy(new TokenAuthorizationStrategy(environmentConfiguration.getRestAuthToken()));
        //TODO use something else as system uer don't have permissions and causes 403 - Unauthorized exception
        client.getCurrentUser(); //make sure client is connected

        runtimeProperties = new HashMap<>();
        String randString = RandomUtils.randString(6);//note the 24 char limit
        buildAgentContextPath = "pnc-ba-" + randString;

        final String buildAgentHost = environmentConfiguration.getBuildAgentHost();

        runtimeProperties.put("build-agent-host", buildAgentHost);
        runtimeProperties.put("pod-name", "pnc-ba-pod-" + randString);
        runtimeProperties.put("service-name", "pnc-ba-service-" + randString);
        runtimeProperties.put("ssh-service-name", "pnc-ba-ssh-" + randString);
        runtimeProperties.put("route-name", "pnc-ba-route-" + randString);
        runtimeProperties.put("route-path", "/" + buildAgentContextPath);
        runtimeProperties.put("buildAgentContextPath", "/" + buildAgentContextPath);
        runtimeProperties.put("containerPort", environmentConfiguration.getContainerPort());

        initDebug();

        ModelNode podConfigurationNode = createModelNode(Configurations.PNC_BUILDER_POD.getContentAsString(), runtimeProperties);
        pod = new Pod(podConfigurationNode, client, ResourcePropertiesRegistry.getInstance().get(OSE_API_VERSION, ResourceKind.POD));
        pod.setNamespace(environmentConfiguration.getPncNamespace());
        Runnable createPod = () -> {
            try {
                client.create(pod, pod.getNamespace());
                podCreated = true;
            } catch (Throwable e) {
                logger.error("Cannot create pod.", e);
            }
        };
        executor.submit(createPod);

        ModelNode serviceConfigurationNode = createModelNode(Configurations.PNC_BUILDER_SERVICE.getContentAsString(), runtimeProperties);
        service = new Service(serviceConfigurationNode, client, ResourcePropertiesRegistry.getInstance().get(OSE_API_VERSION, ResourceKind.SERVICE));
        service.setNamespace(environmentConfiguration.getPncNamespace());
        Runnable createService = () -> {
            try {
                client.create(service, service.getNamespace());
                serviceCreated = true;
            } catch (Throwable e) {
                logger.error("Cannot create service.", e);
            }
        };
        executor.submit(createService);

        if (createRoute) {
            ModelNode routeConfigurationNode = createModelNode(Configurations.PNC_BUILDER_ROUTE.getContentAsString(), runtimeProperties);
            route = new Route(routeConfigurationNode, client, ResourcePropertiesRegistry.getInstance().get(OSE_API_VERSION, ResourceKind.ROUTE));
            route.setNamespace(environmentConfiguration.getPncNamespace());
            Runnable createRoute = () -> {
                try {
                    client.create(route, route.getNamespace());
                    routeCreated = true;
                } catch (Throwable e) {
                    logger.error("Cannot create route.", e);
                }
            };
            executor.submit(createRoute);
        }
    }

    private void initDebug() {
        if (debugData.isEnableDebugOnFailure()) {
            String password = RandomStringUtils.randomAlphanumeric(10);
            debugData.setSshPassword(password);
            runtimeProperties.put(POD_USER_PASSWD, password);

            debugData.setSshServiceInitializer(d -> {
                Integer port = startSshService();
                d.setSshCommand("ssh " + POD_USERNAME + "@" + route.getHost() + " -p " + port);
            });
        }
    }

    private ModelNode createModelNode(String resourceDefinition, Map<String, String> runtimeProperties) {
        String definition = replaceConfigurationVariables(resourceDefinition, runtimeProperties);
        logger.info("Node definition: " + definition);
        return ModelNode.fromJSONString(definition);
    }

    @Override
    public void monitorInitialization(Consumer<RunningEnvironment> onComplete, Consumer<Exception> onError) {

        Consumer<RunningEnvironment> onCompleteInternal = (runningEnvironment) -> {
            logger.info("New build environment available on internal url: {}", getInternalEndpointUrl());

            try {
                Runnable onUrlAvailable = () -> onComplete.accept(runningEnvironment);

                URL url = new URL(getInternalEndpointUrl());
                pullingMonitor.monitor(onUrlAvailable, onError, () -> isServletAvailable(url));
            } catch (IOException e) {
                onError.accept(e);
            }
        };

        pullingMonitor.monitor(onEnvironmentInitComplete(onCompleteInternal, Selector.POD), onError, this::isPodRunning);
        pullingMonitor.monitor(onEnvironmentInitComplete(onCompleteInternal, Selector.SERVICE), onError, this::isServiceRunning);

        logger.info("Waiting to initialize environment. Pod [{}]; Service [{}].", pod.getName(), service.getName());

        if (createRoute) {
            pullingMonitor.monitor(onEnvironmentInitComplete(onComplete, Selector.ROUTE), onError, this::isRouteRunning);
            logger.info("Route [{}].", route.getName());
        }

        //logger.info("Waiting to start a pod [{}], service [{}].", pod.getName(), service.getName());
    }

    private boolean isServletAvailable(URL servletUrl) {
        try {
            return connectToPingUrl(servletUrl);
        } catch (IOException e) {
            return false;
        }
    }

    private Runnable onEnvironmentInitComplete(Consumer<RunningEnvironment> onComplete, Selector selector) {
        return () -> {
            synchronized (this) {
                initialized.add(selector);
                if (createRoute) {
                    if (!initialized.containsAll(Arrays.asList(Selector.POD, Selector.SERVICE, Selector.ROUTE))) {
                        return;
                    }
                } else {
                    if (!initialized.containsAll(Arrays.asList(Selector.POD, Selector.SERVICE))) {
                        return;
                    }
                }
            }

            logger.info("Environment successfully initialized. Pod [{}]; Service [{}].", pod.getName(), service.getName());
            if (createRoute) {
                logger.info("Route [{}].", route.getName());
            }

            RunningEnvironment runningEnvironment = RunningEnvironment.createInstance(
                    pod.getName(),
                    Integer.parseInt(environmentConfiguration.getContainerPort()),
                    route.getHost(),
                    getPublicEndpointUrl(),
                    getInternalEndpointUrl(),
                    repositorySession,
                    Paths.get(environmentConfiguration.getWorkingDirectory()),
                    this::destroyEnvironment,
                    debugData
            );

            onComplete.accept(runningEnvironment);
        };
    }

    private String getPublicEndpointUrl() {
        if (createRoute) {
            return "http://" + route.getHost() + "" + route.getPath() + "/" + environmentConfiguration.getBuildAgentBindPath();
        } else {
            return getInternalEndpointUrl();
        }
    }

    private String getInternalEndpointUrl() {
        return "http://" + service.getPortalIP() + "/" + buildAgentContextPath + "/" + environmentConfiguration.getBuildAgentBindPath();
    }

    private boolean isPodRunning() {
        if (!podCreated) { //avoid Caused by: java.io.FileNotFoundException: https://<host>:8443/api/v1/namespaces/project-ncl/services/pnc-ba-pod-552c
            return false;
        }
        pod = client.get(pod.getKind(), pod.getName(), environmentConfiguration.getPncNamespace());
        boolean isRunning = "Running".equals(pod.getStatus());
        if (isRunning) {
            logger.debug("Pod {} running.", pod.getName());
            return true;
        }
        return false;
    }

    private boolean isServiceRunning() {
        if (!serviceCreated) { //avoid Caused by: java.io.FileNotFoundException: https://<host>:8443/api/v1/namespaces/project-ncl/services/pnc-ba-service-552c
            return false;
        }
        service = client.get(service.getKind(), service.getName(), environmentConfiguration.getPncNamespace());
        boolean isRunning = service.getPods().size() > 0;
        if (isRunning) {
            logger.debug("Service {} running.", service.getName());
            return true;
        }
        return false;
    }

    private boolean isRouteRunning() {
        if (!routeCreated) {
            return false;
        }
        try {
            if (connectToPingUrl(new URL(getPublicEndpointUrl()))) {
                route = client.get(route.getKind(), route.getName(), environmentConfiguration.getPncNamespace());
                logger.debug("Route {} running.", route.getName());
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            logger.error("Cannot open URL " + getPublicEndpointUrl(), e);
            return false;
        }
    }

    @Override
    public String getId() {
        return pod.getName();
    }

    @Override
    public void destroyEnvironment() {
        if (!debugData.isDebugEnabled()) {
            if (!environmentConfiguration.getKeepBuildAgentInstance()) {
                if (createRoute) {
                    client.delete(route);
                }
                client.delete(service);
                if (sshService != null) {
                    client.delete(sshService);
                }
                client.delete(pod);
            }
        }
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
        properties.put("proxyUsername", repositorySession.getBuildRepositoryId());
        properties.put("nonProxyHosts", environmentConfiguration.getNonProxyHosts());

        properties.put("AProxDependencyUrl", repositorySession.getConnectionInfo().getDependencyUrl());
        properties.put("AProxDeployUrl", repositorySession.getConnectionInfo().getDeployUrl());

        properties.putAll(runtimeProperties);

        return StringPropertyReplacer.replaceProperties(podConfiguration, properties);
    }

    /**
     * Enable ssh forwarding
     *
     * @return port, to which ssh is forwarded
     */
    private Integer startSshService() {
        ModelNode serviceConfigurationNode = createModelNode(Configurations.PNC_BUILDER_SSH_SERVICE.getContentAsString(), runtimeProperties);
        sshService = new Service(serviceConfigurationNode, client, ResourcePropertiesRegistry.getInstance().get(OSE_API_VERSION, ResourceKind.SERVICE));
        sshService.setNamespace(environmentConfiguration.getPncNamespace());
        try {
            Service resultService = client.create(this.sshService, sshService.getNamespace());
            return resultService.getNode()
                    .get("spec")
                    .get("ports").asList()
                    .stream()
                    .filter(m -> m.get("name").asString().equals(SSH_SERVICE_PORT_NAME))
                    .findAny().orElseThrow(() -> new RuntimeException("No ssh service in response! Service data: " + describeService(resultService)))
                    .get("nodePort").asInt();
        } catch (Throwable e) {
            logger.error("Cannot create service.", e);
            return null;
        }
    }

    private String describeService(Service resultService) {
        if (resultService == null) return null;

        ModelNode node = resultService.getNode();
        return "Service[" +
                "name = " + resultService.getName() +
                ", node= '" + (node == null ? null : node.toJSONString(false)) +
                "]";
    }

    private enum Selector {
        POD,
        SERVICE,
        ROUTE
    }

    private boolean connectToPingUrl(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(500);
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
