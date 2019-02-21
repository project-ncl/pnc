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

package org.jboss.pnc.rest.restmodel;

/**
 * A Generified interface for all REST Entities.
 *
 * @param <ID> ID type
 */
public interface GenericRestEntity<ID extends Integer> {

    /**
     * Gets Id.
     *
     * @return Id.
     */
    ID getId();

    /**
     * Sets id.
     *
     * @param id id.
     */
    void setId(ID id);
}
