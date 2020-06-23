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
package org.jboss.pnc.integration.utils;

import org.hamcrest.CustomMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static io.restassured.path.json.JsonPath.from;

public class JsonMatcher {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
