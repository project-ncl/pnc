/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.integration.assertions;

import com.jayway.restassured.response.Response;
import org.assertj.core.api.AbstractAssert;

import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ResponseAssertion extends AbstractAssert<ResponseAssertion, Response> {

    protected ResponseAssertion(Response actual) {
        super(actual, ResponseAssertion.class);
    }

    public static ResponseAssertion assertThat(Response response) {
        return new ResponseAssertion(response);
    }

    public ResponseAssertion hasStatus(int httpStatus) {
        assertEquals(httpStatus, actual.statusCode());
        return this;
    }

    public ResponseAssertion hasJsonValueEqual(String jsonKey, int value) {
        assertEquals(value, actual.body().jsonPath().getInt(jsonKey));
        return this;
    }

    public ResponseAssertion hasJsonValueEqual(String jsonKey, String value) {
        assertEquals(value, actual.body().jsonPath().getString(jsonKey));
        return this;
    }

    public ResponseAssertion hasLocationMatches(String matchRegexp) {
        assertTrue(Pattern.matches(matchRegexp, actual.getHeader("Location")));
        return this;
    }

    public ResponseAssertion hasJsonValueNotNullOrEmpty(String jsonKey) {
        String retrievedString = actual.body().jsonPath().getString(jsonKey);
        assertFalse("Expected json node " + jsonKey + " not to be empty", retrievedString == null || retrievedString.isEmpty());
        return this;
    }
}
