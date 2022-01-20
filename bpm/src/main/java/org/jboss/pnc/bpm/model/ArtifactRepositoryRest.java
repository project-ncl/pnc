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

import com.fasterxml.jackson.databind.ObjectMapper;

import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.spi.repositorymanager.ArtifactRepository;

import javax.xml.bind.annotation.XmlRootElement;

import java.io.IOException;

@XmlRootElement(name = "artifactRepository")
public class ArtifactRepositoryRest implements ArtifactRepository {

    private String id;

    private String name;

    private String url;

    private Boolean releases;

    private Boolean snapshots;

    public ArtifactRepositoryRest() {
    }

    public ArtifactRepositoryRest(String serialized) throws IOException {
        ObjectMapper mapper = new ObjectMapper(); // TODO replace with JsonOutputConverterMapper
        ArtifactRepositoryRest artifactRepositoryRestFromJson = mapper
                .readValue(serialized, ArtifactRepositoryRest.class);
        ArtifactRepository artifactRepository = artifactRepositoryRestFromJson.toArtifactRepository();

        init(artifactRepository);
    }

    public ArtifactRepositoryRest(ArtifactRepository artifactRepository) {
        init(artifactRepository);
    }

    private void init(ArtifactRepository buildExecutionConfiguration) {
        id = buildExecutionConfiguration.getId();
        name = buildExecutionConfiguration.getName();
        url = buildExecutionConfiguration.getUrl();
        releases = buildExecutionConfiguration.getReleases();
        snapshots = buildExecutionConfiguration.getSnapshots();
    }

    public ArtifactRepository toArtifactRepository() {
        return ArtifactRepository.build(id, name, url, releases, snapshots);
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public Boolean getReleases() {
        return releases;
    }

    public void setReleases(Boolean releases) {
        this.releases = releases;
    }

    @Override
    public Boolean getSnapshots() {
        return snapshots;
    }

    public void setSnapshots(Boolean snapshots) {
        this.snapshots = snapshots;
    }

    @Override
    public String toString() {
        return JsonOutputConverterMapper.apply(this);
    }

}
