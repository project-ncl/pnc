/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.integration.utils;

import static io.restassured.path.json.JsonPath.from;

import java.util.function.Consumer;
import java.util.stream.Stream;

import org.hamcrest.CustomMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonMatcher {

    private static final Logger logger = LoggerFactory.getLogger(JsonMatcher.class);

    public static CustomMatcher<String> containsJsonAttribute(
            String jsonAttribute,
            Consumer<String>... actionWhenMatches) {
        return new CustomMatcher<String>("matchesJson") {
            @Override
            public boolean matches(Object o) {
                String rawJson = String.valueOf(o);
                logger.debug("Evaluating raw JSON: " + rawJson);
                Object value = from(rawJson).get(jsonAttribute);
                logger.debug("Got value from JSon: " + value);
                if (value != null) {
                    if (actionWhenMatches != null) {
                        Stream.of(actionWhenMatches).forEach(action -> action.accept(String.valueOf(value)));
                    }
                    return true;
                }
                return false;
            }
        };
    }

}
