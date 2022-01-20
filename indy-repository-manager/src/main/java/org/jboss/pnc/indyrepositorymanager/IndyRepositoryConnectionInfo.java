/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.indyrepositorymanager;

import org.jboss.pnc.spi.repositorymanager.model.RepositoryConnectionInfo;

import java.util.HashMap;
import java.util.Map;

public class IndyRepositoryConnectionInfo implements RepositoryConnectionInfo {

    private static final String ALT_DEPLOY_OPTION = "altDeploymentRepository";
    private static final String ALT_DEPLOY_FORMAT = "deploy::default::%s";

    private String url;
    private String deployUrl;

    public IndyRepositoryConnectionInfo(String url, String deployUrl) {
        this.url = url;
        this.deployUrl = deployUrl;
    }

    @Override
    public String getDependencyUrl() {
        return url;
    }

    @Override
    public String getToolchainUrl() {
        return url;
    }

    @Override
    public Map<String, String> getProperties() {
        Map<String, String> props = new HashMap<>();
        props.put(ALT_DEPLOY_OPTION, String.format(ALT_DEPLOY_FORMAT, url));

        return props;
    }

    @Override
    public String getDeployUrl() {
        return deployUrl;
    }

}
