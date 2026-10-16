/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public static <T> T fromJson(String json, Class<T> aClass) throws IOException {
        return objectMapper.readValue(json, aClass);
    }

    public static String toJson(Object objectToBeMapped) throws JsonProcessingException {
        return objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(objectToBeMapped);
    }
}
