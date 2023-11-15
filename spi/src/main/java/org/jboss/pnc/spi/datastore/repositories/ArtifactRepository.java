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
package org.jboss.pnc.spi.datastore.repositories;

import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Interface for manipulating {@link org.jboss.pnc.model.Artifact} entity.
 */
public interface ArtifactRepository extends Repository<Artifact, Integer> {

    Artifact withPurl(String purl);

    Set<Artifact> withIdentifierAndSha256(Collection<Artifact.IdentifierSha256> identifierSha256Set);

    List<Artifact> withSha256AndDependantBuildRecordIdIn(String sha256, Set<Base32LongID> buildRecordIds);

    List<Artifact> withIdentifierAndSha256(String identifier, String sha256);

    List<Artifact> withSha256In(Set<String> sha256);

}
