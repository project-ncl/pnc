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
package org.jboss.pnc.rest.restmodel.bpm;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.RepositoryConfigurationRest;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@JsonDeserialize(builder = RepositoryCreationRest.RepositoryCreationRestBuilder.class)
@AllArgsConstructor
@Builder
@XmlRootElement(name = "RepositoryCreationRest")
@ToString
public class RepositoryCreationRest implements Serializable {

    @Getter
    private final RepositoryConfigurationRest repositoryConfigurationRest;

    @Getter
    private final BuildConfigurationRest buildConfigurationRest;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class RepositoryCreationRestBuilder {
    }

}
