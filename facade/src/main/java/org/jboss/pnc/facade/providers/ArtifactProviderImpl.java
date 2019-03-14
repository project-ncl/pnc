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
package org.jboss.pnc.facade.providers;

import org.jboss.pnc.dto.ArtifactRef;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.mapper.api.ArtifactMapper;
import org.jboss.pnc.facade.providers.api.ArtifactProvider;
import org.jboss.pnc.facade.validation.DTOValidationException;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;

import javax.ejb.Stateless;
import javax.inject.Inject;

import java.util.Optional;

import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withMd5;
import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withSha1;
import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withSha256;
/**
 * @author Jan Michalov <jmichalo@redhat.com>
 */
@Stateless
public class ArtifactProviderImpl extends AbstractProvider<Artifact, org.jboss.pnc.dto.Artifact, ArtifactRef>
        implements ArtifactProvider {

    @Inject
    public ArtifactProviderImpl(ArtifactRepository repository,
            ArtifactMapper mapper) {
        super(repository, mapper, Artifact.class);
    }


    @Override
    public Page<org.jboss.pnc.dto.Artifact> getAll(int pageIndex, int pageSize, String sortingRsql, String query,
            Optional<String> sha256, Optional<String> md5, Optional<String> sha1) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withSha256(sha256), withMd5(md5),withSha1(sha1));
    }

    @Override
    public org.jboss.pnc.dto.Artifact store(org.jboss.pnc.dto.Artifact restEntity) throws DTOValidationException {
        throw new UnsupportedOperationException("Direct artifact manipulation is not available.");
    }

    @Override
    public void update(Integer id, org.jboss.pnc.dto.Artifact restEntity) throws DTOValidationException {
        throw new UnsupportedOperationException("Direct artifact manipulation is not available.");
    }

    @Override
    public void delete(Integer id) throws DTOValidationException {
        throw new UnsupportedOperationException("Direct artifact manipulation is not available.");
    }
}
