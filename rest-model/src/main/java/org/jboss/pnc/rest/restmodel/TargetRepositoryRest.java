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
package org.jboss.pnc.rest.restmodel;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.model.TargetRepository;
import org.jboss.pnc.rest.validation.groups.WhenCreatingNew;
import org.jboss.pnc.rest.validation.groups.WhenUpdating;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@JsonDeserialize(builder = TargetRepositoryRest.TargetRepositoryRestBuilder.class)
@AllArgsConstructor
@Builder
@ToString
@XmlRootElement(name = "TargetRepository")
@NoArgsConstructor
@Setter
public class TargetRepositoryRest implements GenericEntity<Integer> {

    @Getter
    @Setter
    @NotNull(groups = WhenUpdating.class)
    @Null(groups = WhenCreatingNew.class)
    private Integer id;

    /**
     * Flag that the repository is temporary.
     */
    @Getter
    @NotNull(groups = { WhenUpdating.class, WhenCreatingNew.class })
    private Boolean temporaryRepo;

    @Getter
    @NotNull(groups = { WhenUpdating.class, WhenCreatingNew.class })
    private String identifier;

    @Getter
    @NotNull(groups = { WhenUpdating.class, WhenCreatingNew.class })

    private RepositoryType repositoryType;

    @Getter
    @NotNull(groups = { WhenUpdating.class, WhenCreatingNew.class })
    private String repositoryPath;

    @Getter
    @NotNull(groups = { WhenUpdating.class, WhenCreatingNew.class })
    private Set<Integer> artifactIds = new HashSet<>();

    public TargetRepositoryRest(TargetRepository targetRepository) {
        id = targetRepository.getId();
        identifier = targetRepository.getIdentifier();
        repositoryType = targetRepository.getRepositoryType();
        repositoryPath = targetRepository.getRepositoryPath();
        temporaryRepo = targetRepository.getTemporaryRepo();
    }

    public TargetRepository.Builder toDBEntityBuilder() {
        return TargetRepository.newBuilder()
                .id(id)
                .temporaryRepo(temporaryRepo)
                .identifier(identifier)
                .repositoryType(repositoryType)
                .repositoryPath(repositoryPath)
                .artifacts(artifactIds.stream().map(id -> {
                    Artifact artifact = Artifact.Builder.newBuilder().id(id).build();
                    return artifact;
                }).collect(Collectors.toSet()));
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class TargetRepositoryRestBuilder {
    }
}
