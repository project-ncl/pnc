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

package org.jboss.pnc.core.builder.coordinator.bpm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.BpmModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.core.builder.coordinator.BuildScheduler;
import org.jboss.pnc.core.builder.coordinator.BuildSetTask;
import org.jboss.pnc.core.builder.coordinator.BuildTask;
import org.jboss.pnc.core.content.ContentIdentityManager;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.BuildStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@ApplicationScoped
public class BpmBuildScheduler implements BuildScheduler {

    private static final Logger logger = LoggerFactory.getLogger(BpmBuildScheduler.class);

    private static final String REST_START = "/runtime/{0}/process/{1}/start";

    private static final String REST_GET_VAR = "/runtime/{0}/history/instance/{1}/variable/{2}";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private BpmCompleteListener bpmCompleteListener;

    private Configuration configuration;

    @Override
    public String getId() {
        return "bpm-build-scheduler";
    }

    @Deprecated
    public BpmBuildScheduler() { //CDI workaround
    }

    @Inject
    public BpmBuildScheduler(Configuration configuration, BpmCompleteListener bpmCompleteListener) throws MalformedURLException {
        this.bpmCompleteListener = bpmCompleteListener;
        this.configuration = configuration;
    }

    @Override
    public void startBuilding(BuildTask buildTask, Consumer<BuildStatus> onComplete) throws CoreException {
        logger.error("#### 1");
        logger.info("[{}] Triggering BPM process for task", buildTask.getId());
        logger.error("#### 2");
        startProcess(buildTask, Optional.of(buildTask).map(BuildTask::getBuildSetTask).map(BuildSetTask::getId).orElse(null));
        logger.error("#### 3");
        registerCompleteListener(buildTask.getId(), onComplete);
        logger.error("#### 4");
    }

    private void registerCompleteListener(int taskId, Consumer<BuildStatus> onComplete) {
        BpmListener bpmListener = new BpmListener(taskId, onComplete);
        bpmCompleteListener.subscribe(bpmListener);
    }

    private HttpUriRequest createStartProcessRequest(BuildTask buildTask, Integer buildTaskSetId) throws Exception {
        BpmModuleConfig config = configuration.getModuleConfig(new PncConfigProvider<>(BpmModuleConfig.class));
        String processId = config.getProcessId();
        String instanceUrl = config.getBpmInstanceUrl();
        String deploymentId = config.getDeploymentId();
        String bpmEndpointUser = config.getUsername();
        String bpmEndpointPassword = config.getPassword();
        String pncBaseUrl = config.getPncBaseUrl();
        String repourBaseUrl = config.getRepourBaseUrl();
        String daBaseUrl = config.getDaBaseUrl();
        String jenkinsBaseUrl = config.getJenkinsBaseUrl();
        String aproxBaseUrl = config.getAproxBaseUrl();

        StringBuffer url = new StringBuffer(instanceUrl + MessageFormat.format(REST_START, deploymentId, processId));
        url.append("?map_paramsJSON=");
        url.append(URLEncoder.encode(getComponentParametersAsString(buildTask), "UTF-8"));
        url.append("&map_buildRequestJSON=");
        url.append(URLEncoder.encode(getBuildRequestAsString(buildTask, buildTaskSetId), "UTF-8"));
        url.append("&map_pncBaseUrl=");
        url.append(URLEncoder.encode(pncBaseUrl, "UTF-8"));
        url.append("&map_repourBaseUrl=");
        url.append(URLEncoder.encode(repourBaseUrl, "UTF-8"));
        url.append("&map_daBaseUrl=");
        url.append(URLEncoder.encode(daBaseUrl, "UTF-8"));
        url.append("&map_jenkinsBaseUrl=");
        url.append(URLEncoder.encode(jenkinsBaseUrl, "UTF-8"));
        url.append("&map_aproxBaseUrl=");
        url.append(URLEncoder.encode(aproxBaseUrl, "UTF-8"));

        HttpRequestBase startProcessMethod = new HttpPost(url.toString());

        String authString = bpmEndpointUser + ":" + bpmEndpointPassword;
        startProcessMethod.addHeader("Authorization", "Basic " + new String(Base64.encodeBase64(authString.getBytes())));

        logger.error("REST URI: " + url.toString());
        logger.error("AUTH HEADER: " + "Basic " + new String(Base64.encodeBase64(authString.getBytes())));

        return startProcessMethod;
    }

    void startProcess(BuildTask buildTask, Integer buildTaskSetId) throws CoreException {
        try {
            logger.error("#### A");
            HttpClient httpclient = HttpClientBuilder.create().build();
            logger.error("#### B");
            HttpUriRequest startProcessMethod = createStartProcessRequest(buildTask, buildTaskSetId);
            logger.error("#### C");
            HttpResponse response = httpclient.execute(startProcessMethod);
            logger.error("#### D");
//            if(response.getStatusLine().getStatusCode() != 200) {
//                logger.error("#### E");
//                logger.error("Unexpected response code " + response.getStatusLine().getStatusCode() + " with payload " + getResponseBodyAsString(
//                        response));
//                throw new IllegalStateException("Unexpected response code " + response.getStatusLine().getStatusCode() + " with payload " + getResponseBodyAsString(
//                        response));
//            }
            logger.error("#### F");
            String runningProcessId = getProcessId(getResponseBodyAsString(response));
            logger.error("#### G");
            HttpUriRequest checkIfProcessIsRunning = createRunningProcessCheck(runningProcessId);
            logger.error("#### H");

            waitSynchronouslyFor(() -> {
                try {
                    HttpResponse waitResponse = httpclient.execute(checkIfProcessIsRunning);
                    logger.debug("[{}] Waiting REST call returned {}", buildTask.getId(), response.getStatusLine().getStatusCode());
                    return waitResponse.getStatusLine().getStatusCode() == 404;
                } catch (IOException e) {
                    throw new IllegalStateException("Failed while waiting for process", e);
                }
            }, 30, TimeUnit.MINUTES);
            logger.error("#### I");
        } catch (Exception e) {
            throw new CoreException("Failed to invoke BPM Process", e);
        }
    }

    private HttpUriRequest createRunningProcessCheck(String runningProcessId) throws ConfigurationParseException {
        BpmModuleConfig config = configuration.getModuleConfig(new PncConfigProvider<>(BpmModuleConfig.class));
        String instanceUrl = config.getBpmInstanceUrl();
        String deploymentId = config.getDeploymentId();
        String bpmEndpointUser = config.getUsername();
        String bpmEndpointPassword = config.getPassword();

        StringBuffer url = new StringBuffer(instanceUrl);
        url.append(MessageFormat.format(REST_GET_VAR, deploymentId, runningProcessId, "callbackData"));

        HttpRequestBase runningProcessMethod = new HttpGet(url.toString());

        String authString = bpmEndpointUser + ":" + bpmEndpointPassword;
        runningProcessMethod.addHeader("Authorization", "Basic " + new String(Base64.encodeBase64(authString.getBytes())));

        return runningProcessMethod;
    }

    String getComponentParametersAsString(BuildTask buildTask) throws JsonProcessingException {
        /*
        "GAV" : "org.artificer:artificer:1.0.0.Beta1",
        "Description" : "a test",
        "SCM" : "https://github.com/ArtificerRepo/artificer.git",
        "Tag" : "artificer-1.0.0.Beta1-IDP-1",
        "JavaVersion" : "1.7.0_55",
        "MavenVersion" : "3.1.1",
        "BuildCommand" : "mvn -Pgenerate-docs",
        "CommandLineParams" : "-Xmx950m -XX:MaxPermSize=256m -Dmaven.artifact.threads=5",
        "BuildArtifactsRequired" : "JBoss EAP 6.4",
        "CommunityBuild" : "false",
        "PatchBuild" : "false",
        "ProjectId" : 1,
        "EnvironmentId" : 1
         */
        Map<String, String> params = new HashMap<>();
//        params.put("GAV", buildTask.getBuildConfiguration().getProject().getName());
        params.put("GAV", "org.artificer:artificer:1.0.0.Beta2");
        params.put("Description", buildTask.getBuildConfiguration().getDescription());
        params.put("SCM", buildTask.getBuildConfiguration().getScmRepoURL());
        params.put("Tag", buildTask.getBuildConfiguration().getScmRevision());//no such field in PNC, we have only revision
        params.put("JavaVersion", null);//no such field in PNC - We use Environment id...
        params.put("MavenVersion", null);//no such field in PNC
        params.put("BuildCommand", buildTask.getBuildConfiguration().getBuildScript());
        params.put("CommandLineParams", null);//no such field in PNC
        params.put("BuildArtifactsRequired", buildTask.getBuildConfiguration().getAllDependencies().stream()
                .map(bc -> bc.getName())
                .collect(Collectors.toList()).toString());//Is it correct?
        params.put("CommunityBuild", "false");//hardcoded?
        params.put("EnvironmentId", Optional.of(buildTask.getBuildConfiguration())
                .map(BuildConfiguration::getBuildEnvironment)
                .map(BuildEnvironment::getId)
                .map(Object::toString)
                .orElse(""));
        params.put("PatchBuild", "false");//hardcoded?
        params.put("ProjectId", Optional.of(buildTask.getBuildConfiguration())
                .map(BuildConfiguration::getProject)
                .map(Project::getId)
                .map(Object::toString)
                .orElse(""));

        logger.debug("[{}] Component Parameters: {}", buildTask.getId(), params);

        return objectMapper.writeValueAsString(params);
    }

    String getBuildRequestAsString(BuildTask buildTask, Integer buildTaskSetId)
            throws JsonProcessingException {
        Map<String, String> buildRequest = new HashMap<>();
        logger.error("A1");
        buildRequest.put("buildTaskId", Integer.toString(buildTask.getId()));
        logger.error("A2");
        buildRequest.put("buildTaskSetId", Optional.ofNullable(buildTaskSetId).map(Object::toString).orElse(""));
        logger.error("A3");
        buildRequest.put("buildConfigurationRevision",
                Optional.of(buildTask)
                        .map(BuildTask::getBuildConfigurationAudited)
                        .map(BuildConfigurationAudited::getRev)
                        .map(Object::toString)
                        .orElse(""));
        logger.error("A4");
        buildRequest.put("buildRecordSetIdsCSV", StringUtils.toCVS(buildTask.getBuildRecordSetIds()));
        logger.error("A5");
        buildRequest.put("buildConfigSetRecordId",
                Optional.of(buildTask).map(BuildTask::getBuildConfigSetRecordId).map(Object::toString)
                .orElse(""));
        logger.error("A6");
        buildRequest.put("buildContentId", ContentIdentityManager.getBuildContentId(buildTask.getBuildConfiguration()));
        logger.error("A7");
        buildRequest.put("submitTimeMillis", Long.toString(buildTask.getSubmitTime().getTime()));
        logger.error("A8");
        buildRequest.put("pncUsername", Optional.of(buildTask)
                .map(BuildTask::getUser)
                .map(User::getUsername)
                .orElse(null));
        logger.error("A9");
        buildRequest.put("pncUserLoginToken", Optional.of(buildTask)
                .map(BuildTask::getUser)
                .map(User::getLoginToken)
                .orElse(null));
        logger.error("A10");
        buildRequest.put("pncUserEmail", Optional.of(buildTask)
                .map(BuildTask::getUser)
                .map(User::getEmail)
                .orElse(null));
        logger.error("A11");
        logger.debug("[{}] BuildRequest Parameters: {}", buildTask.getId(), buildRequest);

        return objectMapper.writeValueAsString(buildRequest);
    }

    private void waitSynchronouslyFor(Supplier<Boolean> condition, long timeout, TimeUnit timeUnit) {
        long stopTime = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(timeout, timeUnit);
        do {
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                throw new AssertionError("Unexpected interruption", e);
            }
            if(System.currentTimeMillis() > stopTime) {
                throw new AssertionError("Timeout while waiting for condition");
            }
        } while(!condition.get());
    }

    private String getProcessId(String responseBody) {
        int beginIndex = responseBody.indexOf("<id>");
        if (beginIndex < 0) { // not found
            throw new RuntimeException("Can not find process Id");
        }
        beginIndex += 4; // 4, len of "<id>"
        int endIndex = responseBody.indexOf("</id>");
        return responseBody.substring(beginIndex, endIndex);
    }

    private String getResponseBodyAsString(HttpResponse response) throws Exception {
        BufferedReader rd = null;
        try {
            rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        } finally {
            if (rd != null) {
                rd.close();
            }
        }
    }
}
