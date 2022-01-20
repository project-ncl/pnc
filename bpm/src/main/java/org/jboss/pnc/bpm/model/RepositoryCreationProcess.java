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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.dto.BuildConfiguration;

import java.io.Serializable;

/**
 * Repository creation configuration object.
 *
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>.
 * 
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@JsonDeserialize(builder = RepositoryCreationProcess.RepositoryCreationProcessRestBuilder.class)
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Setter
public class RepositoryCreationProcess implements Serializable {

    @Getter
    private RepositoryConfiguration repositoryConfiguration;

    @Getter
    private BuildConfiguration buildConfiguration;

    @Getter
    private String revision;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class RepositoryCreationProcessRestBuilder {
    }

    @Override
    public String toString() {
        return JsonOutputConverterMapper.apply(this);
    }

    @Deprecated // TODO remove for 2.0
    public RepositoryConfiguration getRepositoryConfigurationRest() {
        return repositoryConfiguration;
    }

    @Deprecated // TODO remove for 2.0
    public void setRepositoryConfigurationRest(RepositoryConfiguration repositoryConfigurationRest) {
        this.repositoryConfiguration = repositoryConfigurationRest;
    }

}
