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
package org.jboss.pnc.facade.rsql.converter;

import org.jboss.pnc.mapper.LongBase64IdMapper;
import org.jboss.pnc.mapper.api.IdMapper;

public class Base64EncodedLongValueConverter implements ValueConverter {

    IdMapper<Long, String> idMapper = new LongBase64IdMapper();

    @Override
    public Comparable<Long> convert(Value value) {
        return idMapper.toEntity(value.getValue());
    }
}
