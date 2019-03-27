/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openshift.internal.restclient.model.Pod;
import com.openshift.internal.restclient.model.Route;
import com.openshift.internal.restclient.model.Service;
import com.openshift.internal.restclient.model.properties.ResourcePropertiesRegistry;
import com.openshift.restclient.ClientBuilder;
import com.openshift.restclient.IClient;
import com.openshift.restclient.NotFoundException;
import com.openshift.restclient.ResourceKind;
import com.openshift.restclient.model.IResource;
import org.apache.commons.lang.RandomStringUtils;
import org.jboss.dmr.ModelNode;
import org.jboss.pnc.common.json.moduleconfig.OpenshiftBuildAgentConfig;
import org.jboss.pnc.common.json.moduleconfig.OpenshiftEnvironmentDriverModuleConfig;
import org.jboss.pnc.common.monitor.PullingMonitor;
import org.jboss.pnc.common.monitor.RunningTask;
import org.jboss.pnc.common.util.RandomUtils;
import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.environment.openshift.exceptions.PodFailedStartException;
import org.jboss.pnc.pncmetrics.GaugeMetric;
import org.jboss.pnc.pncmetrics.MetricsConfiguration;
import org.jboss.pnc.spi.builddriver.DebugData;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.jboss.pnc.spi.environment.StartedEnvironment;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.jboss.util.StringPropertyReplacer;
import org.jboss.util.collection.ConcurrentSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.regex.Pattern;


/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class OpenshiftStartedEnvironment implements StartedEnvironment {

    private static final Logger logger = LoggerFactory.getLogger(OpenshiftStartedEnvironment.class);
    private static final String SSH_SERVICE_PORT_NAME = "2222-ssh";
    private static final String POD_USERNAME = "worker";
    private static final String POD_USER_PASSWD = "workerUserPassword";
    private static final String OSE_API_VERSION = "v1";
    private static final Pattern SECURE_LOG_PATTERN = Pattern.compile("\"name\":\\s*\"accessToken\",\\s*\"value\":\\s*\"\\p{Print}+\"");

    private static final String METRICS_POD_STARTED_KEY = "openshift-environment-driver.started.pod";
    private static final String METRICS_POD_STARTED_ATTEMPTED_KEY = METRICS_POD_STARTED_KEY + ".attempts";
    private static final String METRICS_POD_STARTED_SUCCESS_KEY = METRICS_POD_STARTED_KEY + ".success";
    private static final String METRICS_POD_STARTED_FAILED_KEY = METRICS_POD_STARTED_KEY + ".failed";

    private static final int DEFAULT_CREATION_POD_RETRY = 1;

    private int creationPodRetry;

    /**
     * From: https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/
     *
     * ErrImagePull and ImagePullBackOff added to that list. The pod.getStatus() call will return the *reason* of failure,
     * and if the reason is not available, then it'll return the regular status (as mentioned in the link)
     *
     * For pod creation, the failure reason we expect when docker registry is not behaving is 'ErrImagePull' or 'ImagePullBackOff'
     */
    private static final String[] POD_FAILED_STATUSES = {"Failed", "Unknown", "CrashLoopBackOff", "ErrImagePull", "ImagePullBackOff"};


    private boolean serviceCreated = false;
    private boolean podCreated = false;
    private boolean routeCreated = false;

    private final IClient client;
    private final RepositorySession repositorySession;
    private final OpenshiftBuildAgentConfig openshiftBuildAgentConfig;
    private final OpenshiftEnvironmentDriverModuleConfig environmentConfiguration;
    private final PullingMonitor pullingMonitor;
    private final String imageId;
    private final DebugData debugData;
    private final Set<Selector> initialized = new HashSet<>();
    private final Map<String, String> runtimeProperties;

    private final ExecutorService executor;
    private Optional<GaugeMetric> gaugeMetric = Optional.empty();

    private Pod pod;
    private Service service;
    private Route route;
    private Service sshService;

    private ConcurrentSet<RunningTask> runningTaskMonitors = new ConcurrentSet<>();

    private String buildAgentContextPath;

    private final boolean createRoute;

    private Runnable cancelHook;
    private boolean cancelRequested = false;

    private Optional<Future> creatingPod = Optional.empty();
    private Optional<Future> creatingService = Optional.empty();
    private Optional<Future> creatingRoute = Optional.empty();


    public OpenshiftStartedEnvironment(
            ExecutorService executor,
            OpenshiftBuildAgentConfig openshiftBuildAgentConfig,
            OpenshiftEnvironmentDriverModuleConfig environmentConfiguration,
            PullingMonitor pullingMonitor,
            RepositorySession repositorySession,
            String systemImageId,
            DebugData debugData,
            String accessToken,
            boolean tempBuild,
            Date temporaryBuildExpireDate,
            MetricsConfiguration metricsConfiguration) {

        creationPodRetry = DEFAULT_CREATION_POD_RETRY;

        if (environmentConfiguration.getCreationPodRetry() != null) {
            try {
                creationPodRetry = Integer.parseInt(environmentConfiguration.getCreationPodRetry());
            } catch (NumberFormatException e) {
                logger.error("Couldn't parse the value of creation pod retry from the configuration. Using default");
            }
        }

        logger.info("Creating new build environment using image id: " + environmentConfiguration.getImageId());

        this.executor = executor;
        this.openshiftBuildAgentConfig = openshiftBuildAgentConfig;
        this.environmentConfiguration = environmentConfiguration;
        this.pullingMonitor = pullingMonitor;
        this.repositorySession = repositorySession;
        this.imageId = systemImageId == null ? environmentConfiguration.getImageId() : systemImageId;
        this.debugData = debugData;
        if (metricsConfiguration != null) {
            this.gaugeMetric = Optional.of(metricsConfiguration.getGaugeMetric());
        }

        createRoute = environmentConfiguration.getExposeBuildAgentOnPublicUrl();

        client = new ClientBuilder(environmentConfiguration.getRestEndpointUrl())
                .usingToken(environmentConfiguration.getRestAuthToken())
                .build();
        client.getServerReadyStatus(); // make sure client is connected

        runtimeProperties = new HashMap<>();

        final String buildAgentHost = environmentConfiguration.getBuildAgentHost();

        runtimeProperties.put("build-agent-host", buildAgentHost);
        runtimeProperties.put("containerPort", environmentConfiguration.getContainerPort());
        runtimeProperties.put("buildContentId", repositorySession.getBuildRepositoryId());
        runtimeProperties.put("accessToken", accessToken);
        runtimeProperties.put("tempBuild", Boolean.toString(tempBuild));
        runtimeProperties.put("expiresDate", Long.toString(temporaryBuildExpireDate.getTime()));

        createEnvironment();
    }

    private void createEnvironment() {


        String randString = RandomUtils.randString(6);//note the 24 char limit
        buildAgentContextPath = "pnc-ba-" + randString;


        runtimeProperties.put("pod-name", "pnc-ba-pod-" + randString);
        runtimeProperties.put("service-name", "pnc-ba-service-" + randString);
        runtimeProperties.put("ssh-service-name", "pnc-ba-ssh-" + randString);
        runtimeProperties.put("route-name", "pnc-ba-route-" + randString);
        runtimeProperties.put("route-path", "/" + buildAgentContextPath);
        runtimeProperties.put("buildAgentContextPath", "/" + buildAgentContextPath);

        initDebug();

        ModelNode podConfigurationNode = createModelNode(Configurations.getContentAsString(Resource.PNC_BUILDER_POD, openshiftBuildAgentConfig), runtimeProperties);
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
        creatingPod = Optional.of(executor.submit(createPod));

        ModelNode serviceConfigurationNode = createModelNode(Configurations.getContentAsString(Resource.PNC_BUILDER_SERVICE, openshiftBuildAgentConfig), runtimeProperties);
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
        creatingService = Optional.of(executor.submit(createService));

        if (createRoute) {
            ModelNode routeConfigurationNode = createModelNode(Configurations.getContentAsString(Resource.PNC_BUILDER_ROUTE, openshiftBuildAgentConfig), runtimeProperties);
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
            creatingRoute = Optional.of(executor.submit(createRoute));
        }
        gaugeMetric.ifPresent(g -> g.incrementMetric(METRICS_POD_STARTED_ATTEMPTED_KEY));
    }

    static String secureLog(String message) {
        return SECURE_LOG_PATTERN
                .matcher(message)
                .replaceAll("\"name\": \"accessToken\",\n" +
                        "            \"value\": \"***\"");
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
        if (logger.isTraceEnabled()) {
            logger.trace("Node definition: " + secureLog(definition));
        }

        return ModelNode.fromJSONString(definition);
    }

    /**
     * Method to retry creating the whole Openshift environment in case of failure
     *
     * @param e exception thrown
     * @param onComplete consumer to call if successful
     * @param onError consumer to call if no more retries
     * @param retries how many times will we retry starting the build environment
     */
    private void retryPod(Exception e, Consumer<RunningEnvironment> onComplete, Consumer<Exception> onError, int retries) {

        gaugeMetric.ifPresent(g -> g.incrementMetric(METRICS_POD_STARTED_FAILED_KEY));

        logger.debug("Cancelling existing monitors for this build environment");
        cancelAndClearMonitors();

        // no more retries, execute the onError consumer
        if (retries == 0) {
            onError.accept(e);

        } else {
            logger.error("Creating build environment failed! Retrying...");

            // since deletion runs in an executor, it might run *after* the createEnvironment() is finished.
            // createEnvironment()  will overwrite the Openshift object fields. So we need to capture the existing
            // openshift objects to delete before they get overwritten by createEnvironment()
            Route routeToDestroy = route;
            Service serviceToDestroy = service;
            Service sshServiceToDestroy = sshService;
            Pod podToDestroy = pod;

            executor.submit(() -> {
                try {
                    logger.debug("Destroying old build environment");
                    destroyEnvironment(routeToDestroy, serviceToDestroy, sshServiceToDestroy, podToDestroy,true);
                } catch (Exception ex) {
                    logger.error("Error deleting previous environment", ex);
                }
            });

            logger.debug("Creating new build environment");
            createEnvironment();

            // restart the process again
            monitorInitialization(onComplete, onError, retries - 1);
            // at this point the running task running this is finished. New ones are created to monitor pod /service/route creation
        }

    }

    /**
     * Call stack:
     *   monitorInitialization:
     *       -> setup monitors, track them and return
     *
     * -> pullingMonitor.monitor(<pod>) [in background]
     *    -> Success: signal via executing onComplete consumer
     *                finish
     *    -> Failure: call retryPod consumer
     *       -> if retries == 0: call onError consumer. no more retries
     *       -> else: cancel and clear monitors,
     *                delete existing build environment (if any),
     *                recreate build environment,
     *                call monitorInitialization again with retries decremented
     *                finish
     *
     *  While the call stack may appear recursive, it's not in fact recursive due to the fact that we are using RunningTask
     *  to figure out if the pod / route /service are online or not and they run in the background
     */
    @Override
    public void monitorInitialization(Consumer<RunningEnvironment> onComplete, Consumer<Exception> onError) {
        monitorInitialization(onComplete, onError, creationPodRetry);
    }

    /**
     * retries is decremented in retryPod in case of pod failing to start
     *
     * @param onComplete
     * @param onError
     * @param retries
     */
    private void monitorInitialization(Consumer<RunningEnvironment> onComplete, Consumer<Exception> onError, int retries) {

        Consumer<RunningEnvironment> onCompleteInternal = (runningEnvironment) -> {
            logger.info("New build environment available on internal url: {}", getInternalEndpointUrl());

            try {
                Runnable onUrlAvailable = () -> onComplete.accept(runningEnvironment);

                URL url = new URL(getInternalEndpointUrl());
                addMonitors(pullingMonitor.monitor(onUrlAvailable, onError, () -> isServletAvailable(url)));
            } catch (IOException e) {
                onError.accept(e);
            }
        };

        cancelHook = () -> onComplete.accept(null);

        pullingMonitor.monitor(onEnvironmentInitComplete(onCompleteInternal, Selector.POD),
                (t) -> this.retryPod(t, onComplete, onError, retries),
                this::isPodRunning);

        addMonitors(pullingMonitor.monitor(
                onEnvironmentInitComplete(onCompleteInternal, Selector.SERVICE),
                onError,
                this::isServiceRunning));

        logger.info("Waiting to initialize environment. Pod [{}]; Service [{}].", pod.getName(), service.getName());

        if (createRoute) {
            addMonitors(pullingMonitor.monitor(
                            onEnvironmentInitComplete(onCompleteInternal, Selector.ROUTE),
                            onError,
                            this::isRouteRunning));
            logger.info("Route [{}].", route.getName());
        }

        //logger.info("Waiting to start a pod [{}], service [{}].", pod.getName(), service.getName());
    }

    private void addMonitors(RunningTask task) {
        runningTaskMonitors.add(task);
    }

    private void cancelAndClearMonitors() {
        runningTaskMonitors.stream().forEach(pullingMonitor::cancelRunningTask);
        runningTaskMonitors.clear();
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
                logger.info("Route initialized [{}].", route.getName());
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

            gaugeMetric.ifPresent(g -> g.incrementMetric(METRICS_POD_STARTED_SUCCESS_KEY));
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
        return "http://" + service.getClusterIP() + "/" + buildAgentContextPath + "/" + environmentConfiguration.getBuildAgentBindPath();
    }

    /**
     * Check if pod is in running state.
     * If pod is in one of the failure statuses (as specified in POD_FAILED_STATUSES, PodFailedStartException is thrown
     *
     * @return boolean: is pod running?
     */
    private boolean isPodRunning() {
        if (!podCreated) { //avoid Caused by: java.io.FileNotFoundException: https://<host>:8443/api/v1/namespaces/project-ncl/services/pnc-ba-pod-552c
            return false;
        }

        pod = client.get(pod.getKind(), pod.getName(), environmentConfiguration.getPncNamespace());

        String podStatus = pod.getStatus();
        logger.debug("Pod {} status: {}", pod.getName(), podStatus);

        if (Arrays.asList(POD_FAILED_STATUSES).contains(podStatus)) {
            throw new PodFailedStartException("Pod failed with status: " + podStatus);
        }

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
    public void cancel() {
        cancelRequested = true;

        creatingPod.ifPresent(f -> f.cancel(false));
        creatingService.ifPresent(f -> f.cancel(false));
        creatingRoute.ifPresent(f -> f.cancel(false));

        if (cancelHook != null) {
            cancelHook.run();
        } else {
            logger.warn("Trying to cancel operation while no cancel hook is defined.");
        }
        destroyEnvironment();
    }

    @Override
    public void destroyEnvironment() {
        destroyEnvironment(route, service, sshService, pod, false);
    }

    private void destroyEnvironment(Route routeLocal, Service serviceLocal, Service sshServiceLocal,
                                    Pod podLocal, boolean force) {

        if (!debugData.isDebugEnabled() || force) {
            if (!environmentConfiguration.getKeepBuildAgentInstance()) {
                if (createRoute) {
                    tryOpenshiftDeleteResource(routeLocal);
                }
                tryOpenshiftDeleteResource(serviceLocal);
                if (sshService != null) {
                    tryOpenshiftDeleteResource(sshServiceLocal);
                }
                tryOpenshiftDeleteResource(podLocal);
            }
        }
    }

    /**
     * Try to delete an openshift resource. If it doesn't exist, it's fine
     *
     * @param resource Openshift resource to delete
     * @param <T>
     */
    private <T extends IResource> void tryOpenshiftDeleteResource(T resource) {

        try {
            client.delete(resource);
        } catch (NotFoundException e) {
            logger.warn("Couldn't delete the Openshift resource since it does not exist", e);
        }
    }

    private String replaceConfigurationVariables(String podConfiguration, Map runtimeProperties) {
        Boolean proxyActive = !StringUtils.isEmpty(environmentConfiguration.getProxyServer())
                && !StringUtils.isEmpty(environmentConfiguration.getProxyPort());

        Properties properties = new Properties();
        properties.put("image", imageId);
        properties.put("containerPort", environmentConfiguration.getContainerPort());
        properties.put("firewallAllowedDestinations", environmentConfiguration.getFirewallAllowedDestinations());
        // This property sent as Json
        properties.put("allowedHttpOutgoingDestinations", toEscapedJsonString(environmentConfiguration.getAllowedHttpOutgoingDestinations()));
        properties.put("isHttpActive", proxyActive.toString().toLowerCase());
        properties.put("proxyServer", environmentConfiguration.getProxyServer());
        properties.put("proxyPort", environmentConfiguration.getProxyPort());
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
        ModelNode serviceConfigurationNode = createModelNode(Configurations.getContentAsString(Resource.PNC_BUILDER_SSH_SERVICE,
                openshiftBuildAgentConfig), runtimeProperties);
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


    /**
     * Return an escaped string of the JSON representation of the object
     *
     * By 'escaped', it means that strings like '"' are escaped to '\"'
     * @param object object to marshall
     * @return Escaped Json String
     */
    private String toEscapedJsonString(Object object) {
        ObjectMapper mapper = new ObjectMapper();
        JsonStringEncoder jsonStringEncoder = JsonStringEncoder.getInstance();
        try {
            return new String(jsonStringEncoder.quoteAsString(mapper.writeValueAsString(object)));
        } catch(JsonProcessingException e) {
            logger.error("Could not parse object: " + object, e);
            throw new RuntimeException(e);
        }
    }
}
