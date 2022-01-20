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
package org.jboss.pnc.spi.datastore.repositories.api.impl;

import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class DefaultSortInfo implements SortInfo {

    public static final SortingDirection DEFAULT_SORT_DIRECTION = SortingDirection.ASC;

    protected final List<String> fields = new ArrayList<>();
    protected final SortingDirection direction;

    public DefaultSortInfo(SortingDirection direction, String... fields) {
        Collections.addAll(this.fields, fields);
        this.direction = direction;
    }

    public DefaultSortInfo(SortingDirection direction, Collection<String> fields) {
        this.fields.addAll(fields);
        this.direction = direction;
    }

    public DefaultSortInfo() {
        this.direction = DEFAULT_SORT_DIRECTION;
        fields.add("id");
    }

    @Override
    public List<String> getFields() {
        return Collections.unmodifiableList(fields);
    }

    @Override
    public SortingDirection getDirection() {
        return direction;
    }
}
