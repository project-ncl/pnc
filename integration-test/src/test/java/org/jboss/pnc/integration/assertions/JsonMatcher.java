package org.jboss.pnc.integration.assertions;

import org.hamcrest.CustomMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.jayway.restassured.path.json.JsonPath.from;

public class JsonMatcher {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static CustomMatcher<String> containsJsonAttribute(String jsonAttribute, Consumer<String>... actionWhenMatches) {
        return new CustomMatcher<String>("matchesJson") {
            @Override
            public boolean matches(Object o) {
                String rawJson = String.valueOf(o).intern();
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
