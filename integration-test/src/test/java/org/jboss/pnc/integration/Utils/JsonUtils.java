package org.jboss.pnc.integration.Utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonUtils {

    public static <T> T fromJson(String json, Class<T> aClass) throws IOException {
        return new ObjectMapper().readValue(json, aClass);
    }

    public static String toJson(Object objectToBeMapped) throws JsonProcessingException {
        return new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(objectToBeMapped);
    }

}
