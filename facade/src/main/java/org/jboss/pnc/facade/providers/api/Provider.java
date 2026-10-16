/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.providers.api;

import java.io.Serializable;

import org.jboss.pnc.dto.DTOEntity;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.validation.DTOValidationException;
import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;

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
