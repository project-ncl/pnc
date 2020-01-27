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

package org.jboss.pnc.bpm.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Data
@AllArgsConstructor
@Builder(builderClassName = "Builder", builderMethodName = "withCallbackBuilder")
@JsonDeserialize(builder = BuildExecutionConfigurationWithCallbackRest.Builder.class)
@XmlRootElement(name = "buildExecutionConfiguration")
public class BuildExecutionConfigurationWithCallbackRest extends BuildExecutionConfigurationRest {

    private String completionCallbackUrl;

    public BuildExecutionConfigurationWithCallbackRest(BuildExecutionConfiguration buildExecutionConfiguration) {
        super(buildExecutionConfiguration);
    }

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }

}
