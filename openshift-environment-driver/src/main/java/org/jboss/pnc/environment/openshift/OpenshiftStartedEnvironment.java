/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
import com.openshift.restclient.OpenShiftException;
import com.openshift.restclient.ResourceKind;
import com.openshift.restclient.authorization.ResourceForbiddenException;
import com.openshift.restclient.model.IResource;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.apache.commons.lang.RandomStringUtils;
import org.jboss.dmr.ModelNode;
import org.jboss.pnc.common.concurrent.MDCScheduledThreadPoolExecutor;
import org.jboss.pnc.common.json.moduleconfig.OpenshiftBuildAgentConfig;
import org.jboss.pnc.common.json.moduleconfig.OpenshiftEnvironmentDriverModuleConfig;
import org.jboss.pnc.common.logging.MDCUtils;
import org.jboss.pnc.common.monitor.CancellableCompletableFuture;
import org.jboss.pnc.common.monitor.PollingMonitor;
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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
    private static final Pattern SECURE_LOG_PATTERN = Pattern
            .compile("\"name\":\\s*\"accessToken\",\\s*\"value\":\\s*\"\\p{Print}+\"");

    private static final String METRICS_POD_STARTED_KEY = "openshift-environment-driver.started.pod";
    private static final String METRICS_POD_STARTED_ATTEMPTED_KEY = METRICS_POD_STARTED_KEY + ".attempts";
    private static final String METRICS_POD_STARTED_SUCCESS_KEY = METRICS_POD_STARTED_KEY + ".success";
    private static final String METRICS_POD_STARTED_FAILED_KEY = METRICS_POD_STARTED_KEY + ".failed";
    private static final String METRICS_POD_STARTED_RETRY_KEY = METRICS_POD_STARTED_KEY + ".retries";
    private static final String METRICS_POD_STARTED_FAILED_REASON_KEY = METRICS_POD_STARTED_KEY + ".failed_reason";

    /**
     * Max amount of time to wait between retries of creating the environment objects. It has to be greater than 1
     */
    private static final int MAX_CREATION_ENVIRONMENT_RETRY_SECONDS = 5;

    private int creationPodRetry;
    private int pollingMonitorTimeout;
    private int pollingMonitorCheckInterval;

    /**
     * From: https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/
     *
     * ErrImagePull and ImagePullBackOff added to that list. The pod.getStatus() call will return the *reason* of
     * failure, and if the reason is not available, then it'll return the regular status (as mentioned in the link)
     *
     * For pod creation, the failure reason we expect when docker registry is not behaving is 'ErrImagePull' or
     * 'ImagePullBackOff'
     * 
     * 'Error' and 'InvalidImageName' statuses were added as per NCL-6032 investigations
     */
    private static final String[] POD_FAILED_STATUSES = { "Failed", "Unknown", "CrashLoopBackOff", "ErrImagePull",
            "ImagePullBackOff", "Error", "InvalidImageName" };

    /**
     * List of Pod statuses that can be retried
     */
    private static final String[] POD_RETRYABLE_STATUSES = { "Failed", "Unknown", "CrashLoopBackOff", "ErrImagePull",
            "ImagePullBackOff", "Error" };

    /**
     * Parameter specifying override for the builder pod memory size.
     */
    private static final String BUILDER_POD_MEMORY = "BUILDER_POD_MEMORY";

    private final IClient client;
    private final RepositorySession repositorySession;
    private final OpenshiftBuildAgentConfig openshiftBuildAgentConfig;
    private final OpenshiftEnvironmentDriverModuleConfig environmentConfiguration;
    private final PollingMonitor pollingMonitor;
    private final String imageId;
    private final DebugData debugData;
    private final Map<String, String> environmetVariables;

    private final ExecutorService executor;
    private Optional<GaugeMetric> gaugeMetric = Optional.empty();

    private Pod pod;
    private Service service;
    private Route route;
    private Service sshService;

    private ConcurrentSet<CompletableFuture<Void>> runningMonitors = new ConcurrentSet<>();

    private String buildAgentContextPath;

    private final boolean createRoute;

    private Runnable cancelHook;
    private boolean cancelRequested = false;

    private CompletableFuture<Void> creatingPod;
    private CompletableFuture<Void> creatingService;
    private Optional<CompletableFuture<Void>> creatingRoute = Optional.empty();

    // Used to track whether all the futures for creation are completed, or failed with an exception
    private CompletableFuture<Void> openshiftDefinitions;

    private CompletableFuture<Void> createEnvironmentFuture;

    public OpenshiftStartedEnvironment(
            ExecutorService executor,
            OpenshiftBuildAgentConfig openshiftBuildAgentConfig,
            OpenshiftEnvironmentDriverModuleConfig environmentConfiguration,
            PollingMonitor pollingMonitor,
            RepositorySession repositorySession,
            String systemImageId,
            DebugData debugData,
            String accessToken,
            boolean tempBuild,
            Instant temporaryBuildExpireDate,
            MetricsConfiguration metricsConfiguration,
            Map<String, String> parameters) {

        logger.info("Creating new build environment using image id: " + environmentConfiguration.getImageId());

        this.creationPodRetry = environmentConfiguration.getCreationPodRetry();
        this.pollingMonitorTimeout = environmentConfiguration.getPollingMonitorTimeout();
        this.pollingMonitorCheckInterval = environmentConfiguration.getPollingMonitorCheckInterval();
        this.executor = executor;
        this.openshiftBuildAgentConfig = openshiftBuildAgentConfig;
        this.environmentConfiguration = environmentConfiguration;
        this.pollingMonitor = pollingMonitor;
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

        environmetVariables = new HashMap<>();

        final String buildAgentHost = environmentConfiguration.getBuildAgentHost();
        String expiresDateStamp = Long.toString(temporaryBuildExpireDate.toEpochMilli());

        boolean proxyActive = !StringUtils.isEmpty(environmentConfiguration.getProxyServer())
                && !StringUtils.isEmpty(environmentConfiguration.getProxyPort());

        environmetVariables.put("image", imageId);
        environmetVariables
                .put("firewallAllowedDestinations", environmentConfiguration.getFirewallAllowedDestinations());
        // This property sent as Json
        environmetVariables.put(
                "allowedHttpOutgoingDestinations",
                toEscapedJsonString(environmentConfiguration.getAllowedHttpOutgoingDestinations()));
        environmetVariables.put("isHttpActive", Boolean.toString(proxyActive).toLowerCase());
        environmetVariables.put("proxyServer", environmentConfiguration.getProxyServer());
        environmetVariables.put("proxyPort", environmentConfiguration.getProxyPort());
        environmetVariables.put("nonProxyHosts", environmentConfiguration.getNonProxyHosts());

        environmetVariables.put("AProxDependencyUrl", repositorySession.getConnectionInfo().getDependencyUrl());
        environmetVariables.put("AProxDeployUrl", repositorySession.getConnectionInfo().getDeployUrl());

        environmetVariables.put("build-agent-host", buildAgentHost);
        environmetVariables.put("containerPort", environmentConfiguration.getContainerPort());
        environmetVariables.put("buildContentId", repositorySession.getBuildRepositoryId());
        environmetVariables.put("accessToken", accessToken);
        environmetVariables.put("tempBuild", Boolean.toString(tempBuild));
        environmetVariables.put("expiresDate", "ts" + expiresDateStamp);
        MDCUtils.getUserId().ifPresent(v -> environmetVariables.put("logUserId", v));
        MDCUtils.getProcessContext().ifPresent(v -> environmetVariables.put("logProcessContext", v));
        environmetVariables.put("resourcesMemory", builderPodMemory(environmentConfiguration, parameters));

        createEnvironmentWithRetries();
    }

    /**
     * call createEnvironment with retries in case an error happens when talking with Openshift server An example is the
     * dreaded:
     *
     * com.openshift.restclient.OpenShiftException: Exception trying to GET https://<openshift
     * server></openshift>/apis/servicecatalog.k8s.io/v1beta1 response code: 503
     *
     * The max number of retries is `creationPodRetry` with an exponential backoff to a maximum of
     * MAX_CREATION_ENVIRONMENT_RETRY_SECONDS
     *
     */
    private void createEnvironmentWithRetries() {
        RetryPolicy<Object> retryPolicy = new RetryPolicy<>().withMaxRetries(creationPodRetry)
                .withBackoff(1, MAX_CREATION_ENVIRONMENT_RETRY_SECONDS, ChronoUnit.SECONDS);
        createEnvironmentFuture = Failsafe.with(retryPolicy).with(executor).runAsync(this::createEnvironment);
    }

    private void createEnvironment() {

        logger.info("Creating openshift objects...");
        client.getServerReadyStatus(); // make sure client is connected

        String randString = RandomUtils.randString(6);// note the 24 char limit
        buildAgentContextPath = "pnc-ba-" + randString;

        // variables specific to to this pod (retry)
        environmetVariables.put("pod-name", "pnc-ba-pod-" + randString);
        environmetVariables.put("service-name", "pnc-ba-service-" + randString);
        environmetVariables.put("ssh-service-name", "pnc-ba-ssh-" + randString);
        environmetVariables.put("route-name", "pnc-ba-route-" + randString);
        environmetVariables.put("route-path", "/" + buildAgentContextPath);
        environmetVariables.put("buildAgentContextPath", "/" + buildAgentContextPath);

        initDebug();

        ModelNode podConfigurationNode = createModelNode(
                Configurations.getContentAsString(Resource.PNC_BUILDER_POD, openshiftBuildAgentConfig),
                environmetVariables);
        pod = new Pod(
                podConfigurationNode,
                client,
                ResourcePropertiesRegistry.getInstance().get(OSE_API_VERSION, ResourceKind.POD));
        pod.setNamespace(environmentConfiguration.getPncNamespace());
        Runnable createPod = () -> {
            try {
                client.create(pod, pod.getNamespace());
            } catch (Throwable e) {
                logger.error("Cannot create pod.", e);
                throw e;
            }
        };
        creatingPod = CompletableFuture.runAsync(createPod, executor);

        ModelNode serviceConfigurationNode = createModelNode(
                Configurations.getContentAsString(Resource.PNC_BUILDER_SERVICE, openshiftBuildAgentConfig),
                environmetVariables);
        service = new Service(
                serviceConfigurationNode,
                client,
                ResourcePropertiesRegistry.getInstance().get(OSE_API_VERSION, ResourceKind.SERVICE));
        service.setNamespace(environmentConfiguration.getPncNamespace());
        Runnable createService = () -> {
            try {
                client.create(service, service.getNamespace());
            } catch (Throwable e) {
                logger.error("Cannot create service.", e);
                throw e;
            }
        };
        creatingService = CompletableFuture.runAsync(createService, executor);

        if (createRoute) {
            ModelNode routeConfigurationNode = createModelNode(
                    Configurations.getContentAsString(Resource.PNC_BUILDER_ROUTE, openshiftBuildAgentConfig),
                    environmetVariables);
            route = new Route(
                    routeConfigurationNode,
                    client,
                    ResourcePropertiesRegistry.getInstance().get(OSE_API_VERSION, ResourceKind.ROUTE));
            route.setNamespace(environmentConfiguration.getPncNamespace());
            Runnable createRoute = () -> {
                try {
                    client.create(route, route.getNamespace());
                } catch (Throwable e) {
                    logger.error("Cannot create route.", e);
                    throw e;
                }
            };
            CompletableFuture<Void> creatingRouteFuture = CompletableFuture.runAsync(createRoute, executor);
            creatingRoute = Optional.of(creatingRouteFuture);
            openshiftDefinitions = CompletableFuture.allOf(creatingPod, creatingService, creatingRouteFuture);
        } else {
            openshiftDefinitions = CompletableFuture.allOf(creatingPod, creatingService);
        }
        gaugeMetric.ifPresent(g -> g.incrementMetric(METRICS_POD_STARTED_ATTEMPTED_KEY));
    }

    private String builderPodMemory(
            OpenshiftEnvironmentDriverModuleConfig environmentConfiguration1,
            Map<String, String> parameters) {
        double builderPodMemory = environmentConfiguration1.getBuilderPodMemory();
        String builderPodMemoryOverride = parameters.get(BUILDER_POD_MEMORY);
        if (builderPodMemoryOverride != null) {
            try {
                builderPodMemory = Double.parseDouble(builderPodMemoryOverride);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(
                        "Failed to parse memory size '" + builderPodMemoryOverride + "' from " + BUILDER_POD_MEMORY
                                + " parameter.",
                        ex);
            }
            logger.info("Using override for builder pod memory size: " + builderPodMemoryOverride);
        }
        return ((int) Math.ceil(builderPodMemory * 1024)) + "Mi";
    }

    static String secureLog(String message) {
        return SECURE_LOG_PATTERN.matcher(message)
                .replaceAll("\"name\": \"accessToken\",\n" + "            \"value\": \"***\"");
    }

    private void initDebug() {
        if (debugData.isEnableDebugOnFailure()) {
            String password = RandomStringUtils.randomAlphanumeric(10);
            debugData.setSshPassword(password);
            environmetVariables.put(POD_USER_PASSWD, password);

            debugData.setSshServiceInitializer(d -> {
                Integer port = startSshService();
                d.setSshCommand("ssh " + POD_USERNAME + "@" + route.getHost() + " -p " + port);
            });
        }
    }

    private ModelNode createModelNode(String resourceDefinition, Map<String, String> runtimeProperties) {
        Properties properties = new Properties();
        properties.putAll(runtimeProperties);
        String definition = StringPropertyReplacer.replaceProperties(resourceDefinition, properties);
        if (logger.isTraceEnabled()) {
            logger.trace("Node definition: " + secureLog(definition));
        }

        return ModelNode.fromJSONString(definition);
    }

    /**
     * Method to retry creating the whole Openshift environment in case of failure
     *
     * @param onComplete consumer to call if successful
     * @param onError consumer to call if no more retries
     * @param retries how many times will we retry starting the build environment
     */
    private void retryEnvironment(Consumer<RunningEnvironment> onComplete, Consumer<Exception> onError, int retries) {
        gaugeMetric.ifPresent(g -> g.incrementMetric(METRICS_POD_STARTED_FAILED_KEY));
        gaugeMetric.ifPresent(g -> g.incrementMetric(METRICS_POD_STARTED_RETRY_KEY));

        // since deletion runs in an executor, it might run *after* the createEnvironment() is finished.
        // createEnvironment() will overwrite the Openshift object fields. So we need to capture the existing
        // openshift objects to delete before they get overwritten by createEnvironment()
        Route routeToDestroy = route;
        Service serviceToDestroy = service;
        Service sshServiceToDestroy = sshService;
        Pod podToDestroy = pod;

        executor.submit(() -> {
            try {
                logger.debug("Destroying old build environment");
                destroyEnvironment(routeToDestroy, serviceToDestroy, sshServiceToDestroy, podToDestroy, true);
            } catch (Exception ex) {
                logger.error("Error deleting previous environment", ex);
            }
        });

        logger.debug("Creating new build environment");
        createEnvironmentWithRetries();

        // restart the process again
        monitorInitialization(onComplete, onError, retries - 1);
        // at this point the running task running this is finished. New ones are created to monitor pod /service/route
        // creation
    }

    /**
     * Call stack: monitorInitialization: -> setup monitors, track them and return
     *
     * -> pollingMonitor.monitor(<pod>) [in background] -> Success: signal via executing onComplete consumer finish ->
     * Failure: call retryPod consumer -> if retries == 0: call onError consumer. no more retries -> else: cancel and
     * clear monitors, delete existing build environment (if any), recreate build environment, call
     * monitorInitialization again with retries decremented finish
     *
     * While the call stack may appear recursive, it's not in fact recursive due to the fact that we are using
     * RunningTask to figure out if the pod / route /service are online or not and they run in the background
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
    private void monitorInitialization(
            Consumer<RunningEnvironment> onComplete,
            Consumer<Exception> onError,
            int retries) {

        // make sure we run the lambda below after the creation of openshift objects has been submitted
        createEnvironmentFuture.thenAcceptAsync(doNotCare -> {
            cancelHook = () -> onComplete.accept(null);

            CompletableFuture<Void> podFuture = creatingPod.thenComposeAsync(nul -> {
                CancellableCompletableFuture<Void> monitor = pollingMonitor.monitor(
                        this::isPodRunning,
                        pollingMonitorCheckInterval,
                        pollingMonitorTimeout,
                        TimeUnit.SECONDS);
                addFuture(monitor);
                return monitor;
            }, executor);

            CompletableFuture<Void> serviceFuture = creatingService.thenComposeAsync(nul -> {
                CancellableCompletableFuture<Void> monitor = pollingMonitor.monitor(
                        this::isServiceRunning,
                        pollingMonitorCheckInterval,
                        pollingMonitorTimeout,
                        TimeUnit.SECONDS);
                addFuture(monitor);
                return monitor;
            }, executor);

            CompletableFuture<Void> routeFuture;
            if (creatingRoute.isPresent()) {
                routeFuture = creatingRoute.get().thenComposeAsync(nul -> {
                    CancellableCompletableFuture<Void> monitor = pollingMonitor.monitor(
                            this::isRouteRunning,
                            pollingMonitorCheckInterval,
                            pollingMonitorTimeout,
                            TimeUnit.SECONDS);
                    addFuture(monitor);
                    return monitor;
                }, executor);
            } else {
                routeFuture = CompletableFuture.completedFuture(null);
            }

            CompletableFuture<Void> openshiftDefinitionsError = new CompletableFuture<>();
            openshiftDefinitions.exceptionally(t -> {
                openshiftDefinitionsError.completeExceptionally(t);
                return null;
            });

            CancellableCompletableFuture<Void> isBuildAgentUpFuture = pollingMonitor.monitor(
                    () -> isInternalServletAvailable(),
                    pollingMonitorCheckInterval,
                    pollingMonitorTimeout,
                    TimeUnit.SECONDS);
            addFuture(isBuildAgentUpFuture);

            // In the presence of exceptions, the original CompletableFuture#allOf waits for all remaining operations to
            // complete. Instead, if we wanted to signal completion as soon as one of the operations complete
            // exceptionally,
            // we would need to change the implementation, provided in the allOfOrException methos below.
            CompletableFuture<RunningEnvironment> runningEnvironmentFuture = allOfOrException(
                    podFuture,
                    serviceFuture,
                    routeFuture).thenComposeAsync(nul -> isBuildAgentUpFuture)
                            .thenApplyAsync(
                                    nul -> RunningEnvironment.createInstance(
                                            pod.getName(),
                                            Integer.parseInt(environmentConfiguration.getContainerPort()),
                                            route.getHost(),
                                            getPublicEndpointUrl(),
                                            getInternalEndpointUrl(),
                                            repositorySession,
                                            Paths.get(environmentConfiguration.getWorkingDirectory()),
                                            this::destroyEnvironment,
                                            debugData),
                                    executor);

            CompletableFuture.anyOf(runningEnvironmentFuture, openshiftDefinitionsError)
                    .handle((runningEnvironment, throwable) -> {
                        if (throwable != null) {

                            logger.info("Error while trying to create an OpenShift environment... ", throwable);
                            cancelAndClearMonitors();

                            // no more retries, execute the onError consumer
                            if (retries == 0) {
                                logger.info("No more retries left, giving up!");
                                onError.accept(
                                        new Exception(
                                                getPrettierErrorMessageFromThrowable(throwable, true),
                                                throwable));
                            } else {
                                PodFailedStartException podFailedStartExc = null;
                                if (throwable instanceof PodFailedStartException) {
                                    podFailedStartExc = (PodFailedStartException) throwable;
                                } else if (throwable.getCause() instanceof PodFailedStartException) {
                                    podFailedStartExc = (PodFailedStartException) throwable.getCause();
                                }

                                if (podFailedStartExc != null && !Arrays.asList(POD_RETRYABLE_STATUSES)
                                        .contains(podFailedStartExc.getPodStatus())) {

                                    logger.info(
                                            "The detected pod error status '{}' is not considered among the ones to be retried, giving up!",
                                            podFailedStartExc.getPodStatus());
                                    // the status is not to be retried
                                    onError.accept(
                                            new Exception(
                                                    getPrettierErrorMessageFromThrowable(throwable, false),
                                                    throwable));
                                } else {
                                    if (!cancelRequested) {
                                        logger.warn(
                                                "Creating build environment failed with error '{}'! Retrying ({} retries left)...",
                                                throwable,
                                                retries);
                                        retryEnvironment(onComplete, onError, retries);
                                    } else {
                                        logger.info("Build was cancelled, not retrying environment!");
                                    }
                                }
                            }
                        } else {
                            logger.info(
                                    "Environment successfully initialized. Pod [{}]; Service [{}].",
                                    pod.getName(),
                                    service.getName());
                            onComplete.accept((RunningEnvironment) runningEnvironment); // openshiftDefinitionsError
                            // completes only with error
                        }
                        gaugeMetric.ifPresent(g -> g.incrementMetric(METRICS_POD_STARTED_SUCCESS_KEY));
                        return null;
                    });

            logger.info("Waiting to initialize environment. Pod [{}]; Service [{}].", pod.getName(), service.getName());
        }, executor).handle((result, throwable) -> {
            if (throwable != null) {
                onError.accept(new Exception(getPrettierErrorMessageFromThrowable(throwable, false), throwable));
            }
            return null;
        });
    }

    private String getPrettierErrorMessageFromThrowable(Throwable throwable, boolean finishedRetries) {

        String errMsg = "Some errors occurred while trying to create a build environment where to run the build.";

        if (throwable instanceof TimeoutException || throwable.getCause() instanceof TimeoutException) {

            errMsg += " As the maximum timeout has been reached, this could be due to an exhausted capacity of the underlying infrastructure "
                    + "(there is no space available to create the new build environment).";

        } else if (throwable instanceof ResourceForbiddenException
                || throwable.getCause() instanceof ResourceForbiddenException) {

            errMsg += " It looks like the maximum quota available for the build environments has been exceeded.";

        } else if (throwable instanceof PodFailedStartException
                || throwable.getCause() instanceof PodFailedStartException) {

            PodFailedStartException podFailedStartExc = (throwable instanceof PodFailedStartException)
                    ? (podFailedStartExc = (PodFailedStartException) throwable)
                    : (podFailedStartExc = (PodFailedStartException) throwable.getCause());

            if (podFailedStartExc != null && Arrays.asList("ErrImagePull", "ImagePullBackOff", "InvalidImageName")
                    .contains(podFailedStartExc.getPodStatus())) {

                errMsg += " The builder pod failed to start because not able to download the builder image "
                        + "(this could be due to issues with the builder images registry, or a misconfiguration of the builder image name).";
            } else {
                errMsg += " The builder pod failed to start "
                        + "(this could be due to misconfigured or bogus scripts, or other unknown reasons).";
            }
        }

        if (finishedRetries) {
            errMsg += " There are no more retries left (" + (this.creationPodRetry + 1)
                    + " attempts were made), so we are giving up for now!";
        }
        return errMsg;
    }

    public static <T> CompletableFuture<T> allOfOrException(CompletableFuture<T>... futures) {
        CompletableFuture<T> failure = new CompletableFuture<T>();
        for (CompletableFuture<T> f : futures) {
            f.exceptionally(ex -> {
                failure.completeExceptionally(ex);
                return null;
            });
        }
        return (CompletableFuture<T>) CompletableFuture.anyOf(failure, CompletableFuture.allOf(futures));
    }

    private void addFuture(CancellableCompletableFuture<Void> future) {
        runningMonitors.add(future);
    }

    private void cancelAndClearMonitors() {
        logger.debug("Cancelling existing monitors for this build environment");
        runningMonitors.forEach(f -> f.cancel(false));
        runningMonitors.clear();
    }

    private boolean isInternalServletAvailable() {
        try {
            URL servletUrl = new URL(getInternalEndpointUrl());
            logger.debug("isInternalServletAvailable with url: {}", servletUrl);
            return connectToPingUrl(servletUrl);
        } catch (IOException | IllegalArgumentException e) {
            // Illegal argument exception may be thrown if the URL host is null, which may happen if the service hasn't
            // been created yet when getInternalEndpointUrl is called
            return false;
        }
    }

    private String getPublicEndpointUrl() {
        if (createRoute) {
            return "http://" + route.getHost() + "" + route.getPath() + "/"
                    + environmentConfiguration.getBuildAgentBindPath();
        } else {
            return getInternalEndpointUrl();
        }
    }

    private String getInternalEndpointUrl() {
        return "http://" + service.getClusterIP() + "/" + buildAgentContextPath + "/"
                + environmentConfiguration.getBuildAgentBindPath();
    }

    /**
     * Check if pod is in running state. If pod is in one of the failure statuses (as specified in POD_FAILED_STATUSES,
     * PodFailedStartException is thrown
     *
     * @return boolean: is pod running?
     */
    private boolean isPodRunning() {
        pod = client.get(pod.getKind(), pod.getName(), environmentConfiguration.getPncNamespace());

        String podStatus = pod.getStatus();
        logger.debug("Pod {} status: {}", pod.getName(), podStatus);

        if (Arrays.asList(POD_FAILED_STATUSES).contains(podStatus)) {
            gaugeMetric.ifPresent(g -> g.incrementMetric(METRICS_POD_STARTED_FAILED_REASON_KEY + "." + podStatus));
            throw new PodFailedStartException("Pod failed with status: " + podStatus, podStatus);
        }

        boolean isRunning = "Running".equals(pod.getStatus());
        if (isRunning) {
            logger.debug("Pod {} running.", pod.getName());
            return true;
        }
        return false;
    }

    private boolean isServiceRunning() {
        service = client.get(service.getKind(), service.getName(), environmentConfiguration.getPncNamespace());
        boolean isRunning = service.getPods().size() > 0;
        if (isRunning) {
            logger.debug("Service {} running.", service.getName());
            return true;
        }
        return false;
    }

    private boolean isRouteRunning() {
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
        if (creatingPod != null && creatingPod.isDone()) {
            return pod.getName();
        } else {
            return environmetVariables.getOrDefault("pod-name", "");
        }
    }

    @Override
    public void cancel() {
        cancelRequested = true;

        creatingPod.cancel(false);
        creatingService.cancel(false);
        creatingRoute.ifPresent(f -> f.cancel(false));

        cancelAndClearMonitors();

        if (cancelHook != null) {
            cancelHook.run();
        } else {
            logger.warn("Trying to cancel operation while no cancel hook is defined.");
        }
        destroyEnvironment();
    }

    @Override
    public void destroyEnvironment() {
        try {
            destroyEnvironment(route, service, sshService, pod, false);
        } catch (OpenShiftException e) {
            logger.error("Could not destroy the environment.", e);
        }
    }

    private void destroyEnvironment(
            Route routeLocal,
            Service serviceLocal,
            Service sshServiceLocal,
            Pod podLocal,
            boolean force) {

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
            if (resource != null) {
                client.delete(resource);
            }
        } catch (NotFoundException e) {
            logger.warn("Couldn't delete the Openshift resource since it does not exist", e);
        }
    }

    /**
     * Enable ssh forwarding
     *
     * @return port, to which ssh is forwarded
     */
    private Integer startSshService() {
        ModelNode serviceConfigurationNode = createModelNode(
                Configurations.getContentAsString(Resource.PNC_BUILDER_SSH_SERVICE, openshiftBuildAgentConfig),
                environmetVariables);
        sshService = new Service(
                serviceConfigurationNode,
                client,
                ResourcePropertiesRegistry.getInstance().get(OSE_API_VERSION, ResourceKind.SERVICE));
        sshService.setNamespace(environmentConfiguration.getPncNamespace());
        try {
            Service resultService = client.create(this.sshService, sshService.getNamespace());
            return resultService.getNode()
                    .get("spec")
                    .get("ports")
                    .asList()
                    .stream()
                    .filter(m -> m.get("name").asString().equals(SSH_SERVICE_PORT_NAME))
                    .findAny()
                    .orElseThrow(
                            () -> new RuntimeException(
                                    "No ssh service in response! Service data: " + describeService(resultService)))
                    .get("nodePort")
                    .asInt();
        } catch (Throwable e) {
            logger.error("Cannot create service.", e);
            return null;
        }
    }

    private String describeService(Service resultService) {
        if (resultService == null)
            return null;

        ModelNode node = resultService.getNode();
        return "Service[" + "name = " + resultService.getName() + ", node= '"
                + (node == null ? null : node.toJSONString(false)) + "]";
    }

    private boolean connectToPingUrl(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(500);
        connection.setReadTimeout(2000);
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
     * 
     * @param object object to marshall
     * @return Escaped Json String
     */
    private String toEscapedJsonString(Object object) {
        ObjectMapper mapper = new ObjectMapper();
        JsonStringEncoder jsonStringEncoder = JsonStringEncoder.getInstance();
        try {
            return new String(jsonStringEncoder.quoteAsString(mapper.writeValueAsString(object)));
        } catch (JsonProcessingException e) {
            logger.error("Could not parse object: " + object, e);
            throw new RuntimeException(e);
        }
    }
}
