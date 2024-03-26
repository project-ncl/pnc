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

package org.jboss.pnc.bpm.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.spi.coordinator.CompletionStatus;
import org.jboss.pnc.spi.coordinator.ProcessException;
import org.jboss.pnc.spi.environment.EnvironmentDriverResult;
import org.jboss.pnc.spi.repour.RepourResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.io.Serializable;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 *
 * @deprecated use {@link org.jboss.pnc.dto.Build}
 */
@XmlRootElement(name = "buildResult")
@NoArgsConstructor
@AllArgsConstructor
@Deprecated
public class BuildResultRest extends BpmEvent implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(BuildResultRest.class);

    @NotNull(groups = WhenCreatingNew.class)
    @Getter
    @Setter
    private CompletionStatus completionStatus;

    @Getter
    @Setter
    private ProcessException processException;

    @Getter
    @Setter
    private BuildExecutionConfigurationRest buildExecutionConfiguration;

    @Getter
    @Setter
    private BuildDriverResultRest buildDriverResult;

    @Getter
    @Setter
    private RepositoryManagerResultRest repositoryManagerResult;

    @Getter
    @Setter
    private EnvironmentDriverResult environmentDriverResult;

    @Getter
    @Setter
    private RepourResult repourResult;

    public static BuildResultRest valueOf(String serialized) throws IOException {
        return JsonOutputConverterMapper.readValue(serialized, BuildResultRest.class);
    }

    @Override
    public String getEventType() {
        return "BUILD_COMPLETE";
    }

    @Override
    public String toString() {
        return "BuildResultRest{" + "completionStatus=" + completionStatus + ", processException=" + processException
                + '\'' + ", buildExecutionConfiguration=" + buildExecutionConfiguration + ", buildDriverResult="
                + (buildDriverResult == null ? null : buildDriverResult.toStringLimited())
                + ", repositoryManagerResult="
                + (repositoryManagerResult == null ? null : repositoryManagerResult.toStringLimited())
                + ", environmentDriverResult="
                + (environmentDriverResult == null ? null : environmentDriverResult.toStringLimited())
                + ", repourResult=" + (repourResult == null ? null : repourResult.toStringLimited()) + '}';
    }

    public String toFullLogString() {
        return JsonOutputConverterMapper.apply(this);
    }

}
