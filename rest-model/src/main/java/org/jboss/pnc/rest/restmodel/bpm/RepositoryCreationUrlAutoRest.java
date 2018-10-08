/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.rest.validation.groups.WhenCreatingNew;
import org.jboss.pnc.rest.validation.groups.WhenUpdating;

import java.io.Serializable;

/**
 * Repository creation configuration object used by endpoint that automatically detects internal vs external url.
 *
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>.
 */
@JsonDeserialize(builder = RepositoryCreationUrlAutoRest.RepositoryCreationUrlAutoRestBuilder.class)
@AllArgsConstructor
@Builder
@NoArgsConstructor(onConstructor=@__({@Deprecated}))
public class RepositoryCreationUrlAutoRest implements Serializable {

    @Getter
    @Setter(onMethod=@__({@Deprecated}))
    @NotBlank(groups = {WhenUpdating.class, WhenCreatingNew.class})
    private String scmUrl;

    @Getter
    @Setter(onMethod=@__({@Deprecated}))
    private Boolean preBuildSyncEnabled;

    @Getter
    @Setter(onMethod=@__({@Deprecated}))
    private BuildConfigurationRest buildConfigurationRest;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class RepositoryCreationUrlAutoRestBuilder {
    }

    @Override
    public String toString() {
        return JsonOutputConverterMapper.apply(this);
    }
}
