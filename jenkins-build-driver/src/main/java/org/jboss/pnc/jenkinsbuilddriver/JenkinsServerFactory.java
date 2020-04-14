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
package org.jboss.pnc.jenkinsbuilddriver;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.client.JenkinsHttpClient;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.JenkinsBuildDriverModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Factory to produce Jenkins server connection.
 * Each server configuration (each build) has its own factory.
 *
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-23.
 */
public class JenkinsServerFactory {

    @Inject
    Configuration configuration;

    JenkinsServer getJenkinsServer(String url) throws BuildDriverException {
        try {
            JenkinsBuildDriverModuleConfig config = configuration
                    .getModuleConfig(new PncConfigProvider<JenkinsBuildDriverModuleConfig>(JenkinsBuildDriverModuleConfig.class));
            String username = config.getUsername();
            String password = config.getPassword();

            if (url == null || username == null || password == null) {
                throw new BuildDriverException("Missing config to instantiate " + JenkinsBuildDriver.DRIVER_ID + ".");
            }
            try {
                return new JenkinsServer(new URI(url), username, password);
            } catch (URISyntaxException e) {
                throw new BuildDriverException("Cannot instantiate " + JenkinsBuildDriver.DRIVER_ID + ". Make sure you are using valid url: " + url, e);
            }
        } catch (ConfigurationParseException e) {
            throw new BuildDriverException("Cannot read configuration for " + JenkinsBuildDriver.DRIVER_ID + ".", e);        }
    }
    
    /**
     * This checks if jenkins does not use option Prevent Cross Site Request Forgery exploits
     * 
     * mnovotny: TODO: see NCL-669 this method should be placed in producing JenkinsServer, but as CSRF is not propagated
     * out from JenkinsServer instance, we need to figure out the setting by special API call through JenkinsHttpClient
     *  
     * @param url Jenkins instance URL with port
     * @return
     * @throws BuildDriverException
     */
    boolean isJenkinsServerSecuredWithCSRF(String url) throws BuildDriverException {
        try {
            JenkinsBuildDriverModuleConfig config = configuration
                    .getModuleConfig(new PncConfigProvider<JenkinsBuildDriverModuleConfig>(JenkinsBuildDriverModuleConfig.class));
            String username = config.getUsername();
            String password = config.getPassword();

            if (url == null || username == null || password == null) {
                throw new BuildDriverException("Missing config to instantiate " + JenkinsBuildDriver.DRIVER_ID + ".");
            }
            try {
                JenkinsHttpClient jenkinsHttpClient = new JenkinsHttpClient(new URI(url), username, password);
                try {
                    jenkinsHttpClient.get("/crumbIssuer/api/xml");
                    return true ;
                } catch (IOException e) {
                    return false;
                }
            } catch (URISyntaxException e) {
                throw new BuildDriverException("Cannot instantiate " + JenkinsBuildDriver.DRIVER_ID + ". Make sure you are using valid url: " + url, e);
            }
        } catch (ConfigurationParseException e) {
            throw new BuildDriverException("Cannot read configuration for " + JenkinsBuildDriver.DRIVER_ID + ".", e);        }
        
    }
}
