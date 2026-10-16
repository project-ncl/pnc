/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.json;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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
