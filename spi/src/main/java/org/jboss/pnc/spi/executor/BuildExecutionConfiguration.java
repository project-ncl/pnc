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

package org.jboss.pnc.spi.executor;

import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.enums.BuildType;
import org.jboss.pnc.enums.SystemImageType;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.spi.repositorymanager.ArtifactRepository;
import org.jboss.pnc.spi.repositorymanager.BuildExecution;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public interface BuildExecutionConfiguration extends BuildExecution {

    @Override
    String getId();

    String getUserId();

    String getBuildScript();

    String getBuildConfigurationId();

    String getName(); // used to be buildConfiguration.name

    String getScmRepoURL();

    String getScmRevision();

    String getScmTag();

    String getScmBuildConfigRevision();

    Boolean isScmBuildConfigRevisionInternal();

    String getOriginRepoURL();

    boolean isPreBuildSyncEnabled();

    String getSystemImageId();

    String getSystemImageRepositoryUrl();

    SystemImageType getSystemImageType();

    boolean isPodKeptOnFailure();

    Map<String, String> getGenericParameters();

    String getDefaultAlignmentParams();

    AlignmentPreference getAlignmentPreference();

}
