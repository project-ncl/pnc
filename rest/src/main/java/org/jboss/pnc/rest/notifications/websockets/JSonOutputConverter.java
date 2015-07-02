/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.rest.notifications.websockets;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.pnc.spi.notifications.OutputConverter;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JSonOutputConverter implements OutputConverter {

    private ObjectMapper mapper = new ObjectMapper();

    public JSonOutputConverter() {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public String apply(Object objectToBeConverted) {
        if(objectToBeConverted != null) {
            try {
                return mapper.writeValueAsString(objectToBeConverted);
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Could not convert object to JSON", e);
            }
        }
        return "{}";
    }
}
