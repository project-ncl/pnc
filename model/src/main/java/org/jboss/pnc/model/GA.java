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

package org.jboss.pnc.model;

import javax.persistence.Embeddable;
import javax.validation.constraints.Pattern;

/**
 * Group ID and Artifact ID
 *
 * @author Sebastian Laskawiec
 */
@Embeddable
public class GA {

    @Pattern(regexp = "[a-zA-Z_0-9-\\.]")
    String groupId;

    @Pattern(regexp = "[a-zA-Z_0-9-\\.]")
    String artifactId;

    public GA(String groupId, String artifactId) {
        if(groupId == null || groupId.isEmpty()) {
            throw new IllegalArgumentException("Group Id can not be null or empty");
        }

        if(artifactId == null || artifactId.isEmpty()) {
            throw new IllegalArgumentException("Artifact Id can not be null or empty");
        }

        this.groupId = groupId;
        this.artifactId = artifactId;
    }

    public GA() {
    }

    public GA(String gaAsString) {
        if(gaAsString == null || gaAsString.isEmpty()) {
            throw new IllegalArgumentException("GA can not be null or empty");
        }
        String [] splittedGa = gaAsString.split(":");
        if(splittedGa.length != 2) {
            throw new IllegalArgumentException("GA is in incorrect format");
        }
        this.groupId = splittedGa[0];
        this.artifactId = splittedGa[1];
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    /**
     * @return Returns GA format: <code>groupId:artifactId</code>
     */
    @Override
    public String toString() {
        return groupId + ":" + artifactId;
    }
}
