package org.jboss.pnc.integration.matchers;

import com.jayway.restassured.response.Response;
import org.assertj.core.api.AbstractAssert;

import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
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

}
