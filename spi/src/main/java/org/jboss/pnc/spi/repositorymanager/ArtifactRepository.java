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

public interface ArtifactRepository {

    String getId();

    String getName();

    String getUrl();

    Boolean getReleases();

    Boolean getSnapshots();

    static ArtifactRepository build(String id, String name, String url, Boolean releases, Boolean snapshots) {

        return new ArtifactRepository() {

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
        };
    }

}
