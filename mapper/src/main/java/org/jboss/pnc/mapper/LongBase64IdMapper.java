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

import org.jboss.pnc.common.Numbers;
import org.jboss.pnc.mapper.api.IdMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LongBase64IdMapper implements IdMapper<Long, String> {

    private static final Logger logger = LoggerFactory.getLogger(LongBase64IdMapper.class);

    @Override
    public Long toEntity(String id) {
        if (id.matches("[0-9]+")) {
            try {
                long possibleOldId = Long.parseLong(id);
                // backward compatibility to support old urls
                // GUID are always bigger than
                if (possibleOldId < 100000000000000000L) {
                    return possibleOldId;
                }
            } catch (NumberFormatException e) {
                // not a long number
                logger.warn("Id is not a long.", e);
            }
        }
        return Numbers.base64ToDecimal(id);
    }

    @Override
    public String toDto(Long id) {
        // backward compatibility to support old ids
        // GUID are always bigger than
        if (id < 100000000000000000L) {
            return id.toString();
        } else {
            return Numbers.decimalToBase64(id);
        }
    }
}
