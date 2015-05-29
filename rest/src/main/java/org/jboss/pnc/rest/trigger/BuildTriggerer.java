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
package org.jboss.pnc.rest.trigger;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import org.jboss.logging.Logger;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.BpmModuleConfig;
import org.jboss.pnc.core.builder.BuildCoordinator;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.core.notifications.buildSetTask.BuildSetCallBack;
import org.jboss.pnc.core.notifications.buildSetTask.BuildSetStatusNotifications;
import org.jboss.pnc.core.notifications.buildTask.BuildCallBack;
import org.jboss.pnc.core.notifications.buildTask.BuildStatusNotifications;
import org.jboss.pnc.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.datastore.repositories.BuildConfigurationSetRepository;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerException;

import com.google.common.base.Preconditions;

import javax.ejb.Stateless;
import javax.inject.Inject;

import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.function.Consumer;

@Stateless
public class BuildTriggerer {

    private final Logger log = Logger.getLogger(BuildTriggerer.class);
    
    private BuildCoordinator buildCoordinator;
    private BuildConfigurationRepository buildConfigurationRepository;
    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;
    private BuildConfigurationSetRepository buildConfigurationSetRepository;
    private BuildSetStatusNotifications buildSetStatusNotifications;
    private BuildStatusNotifications buildStatusNotifications;

    private BpmModuleConfig bpmConfig = null;
    
    @Deprecated //not meant for usage its only to make CDI happy
    public BuildTriggerer() {
    }

    @Inject
    public BuildTriggerer(final BuildCoordinator buildCoordinator,
                          final BuildConfigurationRepository buildConfigurationRepository,
                          final BuildConfigurationAuditedRepository buildConfigurationAuditedRepository,
                          final BuildConfigurationSetRepository buildConfigurationSetRepository,
                          BuildSetStatusNotifications buildSetStatusNotifications,
                          BuildStatusNotifications buildStatusNotifications) {
        this.buildCoordinator = buildCoordinator;
        this.buildConfigurationRepository = buildConfigurationRepository;
        this.buildConfigurationAuditedRepository = buildConfigurationAuditedRepository;
        this.buildConfigurationSetRepository= buildConfigurationSetRepository;
        this.buildSetStatusNotifications = buildSetStatusNotifications;
        this.buildStatusNotifications = buildStatusNotifications;
    }

    public int triggerBuilds( final Integer buildConfigurationId, User currentUser, URL callBackUrl)
            throws InterruptedException, CoreException, BuildDriverException, RepositoryManagerException
    {
        Consumer<BuildStatusChangedEvent> onStatusUpdate = (statusChangedEvent) -> {
            // Expecting URL like: http://host:port/business-central/rest/runtime/org.test:Test1:1.0/process/instance/7/signal?signal=testSig
            signalBpmEvent(callBackUrl.toString() + "&event=" + statusChangedEvent.getNewStatus());
        };

        int buildTaskId = triggerBuilds(buildConfigurationId, currentUser);
        buildStatusNotifications.subscribe(new BuildCallBack(buildTaskId, onStatusUpdate));
        return buildTaskId;
    }

    public int triggerBuilds( final Integer configurationId, User currentUser )
        throws InterruptedException, CoreException, BuildDriverException, RepositoryManagerException
    {
        final BuildConfiguration configuration = buildConfigurationRepository.findOne(configurationId);
        configuration.setBuildConfigurationAudited(this.getLatestAuditedBuildConfiguration(configurationId));

        Preconditions.checkArgument(configuration != null, "Can't find configuration with given id=" + configurationId);

        final BuildRecordSet buildRecordSet = new BuildRecordSet();
        if (configuration.getProductVersions() != null  && !configuration.getProductVersions().isEmpty()) {
            ProductVersion productVersion = configuration.getProductVersions().iterator().next();
            buildRecordSet.setProductMilestone(productVersion.getCurrentProductMilestone());
        }

        Integer taskId = buildCoordinator.build(configuration, currentUser).getBuildConfiguration().getId();
        return taskId;
    }

    public int triggerBuildConfigurationSet( final Integer buildConfigurationSetId, User currentUser, URL callBackUrl)
        throws InterruptedException, CoreException, BuildDriverException, RepositoryManagerException
    {
        Consumer<BuildSetStatusChangedEvent> onStatusUpdate = (statusChangedEvent) -> {
            // Expecting URL like: http://host:port/business-central/rest/runtime/org.test:Test1:1.0/process/instance/7/signal?signal=testSig
            signalBpmEvent(callBackUrl.toString() + "&event=" + statusChangedEvent.getNewStatus());
        };

        int buildSetTaskId = triggerBuildConfigurationSet(buildConfigurationSetId, currentUser);
        buildSetStatusNotifications.subscribe(new BuildSetCallBack(buildSetTaskId, onStatusUpdate));
        return buildSetTaskId;
    }

    public int triggerBuildConfigurationSet( final Integer buildConfigurationSetId, User currentUser )
        throws InterruptedException, CoreException, BuildDriverException, RepositoryManagerException
    {
        final BuildConfigurationSet buildConfigurationSet = buildConfigurationSetRepository.findOne(buildConfigurationSetId);
        Preconditions.checkArgument(buildConfigurationSet != null, "Can't find configuration with given id=" + buildConfigurationSetId);

        for (BuildConfiguration config : buildConfigurationSet.getBuildConfigurations()) {
            config.setBuildConfigurationAudited(this.getLatestAuditedBuildConfiguration(config.getId()));
        }

        return buildCoordinator.build(buildConfigurationSet, currentUser).getId();
    }

    /**
     * Get the latest audited revision for the given build configuration ID
     * 
     * @param buildConfigurationId
     * @return The latest revision of the given build configuration
     */
    private BuildConfigurationAudited getLatestAuditedBuildConfiguration(Integer buildConfigurationId) {
        List<BuildConfigurationAudited> buildConfigRevs = buildConfigurationAuditedRepository.findAllByIdOrderByRevDesc(buildConfigurationId);
        if ( buildConfigRevs.isEmpty() ) {
            // TODO should we throw an exception?  This should never happen.
            return null;
        }
        return buildConfigRevs.get(0);
    }
    
    /*
     * TODO: Do not ignore certificates, rather setup servers properly.
     */
    private void signalBpmEvent(String uri) {
        if (bpmConfig == null) {
            try {
                bpmConfig = new Configuration().getModuleConfig(BpmModuleConfig.class);
            } catch (ConfigurationParseException e) {
                log.error("Error parsing BPM config.", e);
            }
        }
        
        SSLContextBuilder builder = new SSLContextBuilder();
        try {
            builder.loadTrustMaterial(null, new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            });
        } catch (NoSuchAlgorithmException | KeyStoreException e1) {
            e1.printStackTrace();
        }

        SSLConnectionSocketFactory sslSF = null;
        try {
            sslSF = new SSLConnectionSocketFactory(builder.build(), SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        } catch (KeyManagementException | NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }

        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslSF)
                .setHostnameVerifier(new AllowAllHostnameVerifier()).build();

        HttpPost request = new HttpPost(uri);
        request.addHeader("Authorization", getAuthHeader());
        log.info("Executing request " + request.getRequestLine());

        try (CloseableHttpResponse response = httpclient.execute(request)) {
            log.info(response.getStatusLine());
            response.close();
            httpclient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getAuthHeader() {
        byte[] encodedBytes = Base64.encodeBase64((bpmConfig.getUsername() + ":" + bpmConfig.getPassword()).getBytes());
        return "Basic " + new String(encodedBytes);
    }
}
