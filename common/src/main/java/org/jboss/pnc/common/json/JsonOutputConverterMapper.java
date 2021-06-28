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

package org.jboss.pnc.common.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class JsonOutputConverterMapper {

    public final static Logger log = LoggerFactory.getLogger(JsonOutputConverterMapper.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.registerModule(new Jdk8Module());
        mapper.registerModule(new JavaTimeModule());

        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS);
    }

    /**
     *
     * @throws RuntimeException
     */
    public static String apply(Object objectToBeConverted) {
        if (objectToBeConverted != null) {
            try {
                return mapper.writeValueAsString(objectToBeConverted);
            } catch (JsonProcessingException e) {
                log.warn("Could not convert object to JSON", e);
                throw new IllegalArgumentException("Could not convert object to JSON", e);
            }
        }
        return "{}";
    }

    public static <T> T readValue(String serialized, Class<T> clazz) throws IOException {
        return mapper.readValue(serialized, clazz);
    }

    public static <T> T readValue(InputStream serialized, Class<T> clazz) throws IOException {
        return mapper.readValue(serialized, clazz);
    }

    static final class OptionalMixin {
        private OptionalMixin() {
        }

        @JsonProperty
        private Object value;
    }

    public static ObjectMapper getMapper() {
        return mapper;
    }
}
