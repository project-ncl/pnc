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
package org.jboss.pnc.mapper;

import org.jboss.pnc.mapper.api.IdMapper;
import org.jboss.pnc.model.Base32LongID;

public class Base32LongIdMapper implements IdMapper<Base32LongID, String> {

    @Override
    public Base32LongID toEntity(String id) {
        return new Base32LongID(id);
    }

    @Override
    public String toDto(Base32LongID id) {
        return id.getId();
    }
}
