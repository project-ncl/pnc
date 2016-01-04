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

package org.jboss.pnc.spi.executor;

import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.BuildExecutionStatus;
import org.jboss.pnc.spi.events.BuildCoordinationStatusChangedEvent;
import org.jboss.pnc.spi.repositorymanager.BuildExecution;

import java.net.URI;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public interface BuildExecutionConfiguration extends BuildExecution {

    BuildConfiguration getBuildConfiguration();

    BuildConfigurationAudited getBuildConfigurationAudited();

    int getId();

    User getUser();

    static BuildExecutionConfiguration build(
            int id,
            BuildConfiguration buildConfiguration,
            BuildConfigurationAudited buildConfigurationAudited,
            String buildContentId,
            String projectName,
            User user) {
        return new BuildExecutionConfiguration() {

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
                return projectName;
            }

            @Override
            public User getUser() {
                return user;
            }
        };
    }
}
