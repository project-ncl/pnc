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
package org.jboss.pnc.facade.rsql.mapper;

import org.jboss.pnc.facade.rsql.RSQLSelectorPath;
import org.jboss.pnc.facade.rsql.converter.ValueConverter;
import org.jboss.pnc.model.GenericEntity;

import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import java.io.Serializable;

/**
 * Mappers that converts RSQL path with DTO field names to Criteria API path.
 * 
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 * @param <DB> The database entity type
 */
public interface RSQLMapper<ID extends Serializable, DB extends GenericEntity<ID>> {

    /**
     * Return the class for which this mapper works.
     * 
     * @return
     */
    Class<DB> type();

    /**
     * Converts RSQL selector to Criteria API path.
     * 
     * @param from The entity path node.
     * @param selector The RSQL selector.
     * @return Criteria API path equivalent of the RSQL selector.
     */
    Path<?> toPath(From<?, DB> from, RSQLSelectorPath selector);

    /**
     * Converts RSQL selector to string with '.' separetaed list of entity field names.
     * 
     * @param selector The RSQL selector
     * @return String with entity field names separated by '.'.
     */
    String toPath(RSQLSelectorPath selector);

    ValueConverter getValueConverter(String name);
}
