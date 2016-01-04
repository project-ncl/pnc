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

package org.jboss.pnc.executor;

import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class DefaultBuildExecutionConfiguration implements BuildExecutionConfiguration {

    public static final Logger log = LoggerFactory.getLogger(DefaultBuildExecutionConfiguration.class);

    private int id;
    private BuildConfiguration buildConfiguration;
    private BuildConfigurationAudited buildConfigurationAudited;
    private String buildContentId;
    private User user;
    private Integer buildTaskId;

    public DefaultBuildExecutionConfiguration( //TODO and extract task methods in DefaultBuildExecutionTask
                                               int id,
                                               BuildConfiguration buildConfiguration,
                                               BuildConfigurationAudited buildConfigurationAudited,
                                               String buildContentId,
                                               User user,
                                               Integer buildTaskId) {
        this.id = id;
        this.buildConfiguration = buildConfiguration;
        this.buildConfigurationAudited = buildConfigurationAudited;
        this.buildContentId = buildContentId;
        this.user = user;
        this.buildTaskId = buildTaskId;
    }


    @Override
    public BuildConfiguration getBuildConfiguration() {
        return buildConfiguration;
    }

    @Override
    public BuildConfigurationAudited getBuildConfigurationAudited() {
        return buildConfigurationAudited;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getBuildContentId() {
        return buildContentId;
    }

    @Override
    public String getProjectName() {
        Project project = getBuildConfiguration().getProject();
        if (project != null) {
            return project.getName();
        } else {
            return "-- no associated project --";
        }
    }

    @Override
    public User getUser() {
        return user;
    }
}
