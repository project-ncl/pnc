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
package org.jboss.pnc.spi.repositorymanager;

public class DefaultArtifactRepository implements ArtifactRepository {
    private final String id;

    private final String name;

    private final String url;

    private final Boolean releases;

    private final Boolean snapshots;

    public DefaultArtifactRepository(String id, String name, String url, Boolean releases, Boolean snapshots) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.releases = releases;
        this.snapshots = snapshots;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public Boolean getReleases() {
        return releases;
    }

    @Override
    public Boolean getSnapshots() {
        return snapshots;
    }
}
