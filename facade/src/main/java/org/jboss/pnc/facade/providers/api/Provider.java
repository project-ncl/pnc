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
package org.jboss.pnc.facade.providers.api;

import org.jboss.pnc.dto.DTOEntity;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.validation.DTOValidationException;
import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;

import java.io.Serializable;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 * @param <DB> The database entity type
 * @param <DTO> The full DTO entity type
 * @param <REF> The reference DTO entity type
 */
public interface Provider<ID extends Serializable, DB extends GenericEntity<ID>, DTO extends REF, REF extends DTOEntity> {

    DTO store(DTO restEntity) throws DTOValidationException;

    DTO getSpecific(String id);

    Page<DTO> getAll(int pageIndex, int pageSize, String sortingRsql, String query);

    DTO update(String id, DTO restEntity) throws DTOValidationException;

    void delete(String id) throws DTOValidationException;

    Page<DTO> queryForCollection(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            Predicate<DB>... predicates);

}
