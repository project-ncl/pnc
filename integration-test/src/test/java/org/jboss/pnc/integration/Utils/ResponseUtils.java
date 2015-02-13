package org.jboss.pnc.integration.Utils;

import com.jayway.restassured.response.Response;

public class ResponseUtils {

    public static Integer getIdFromLocationHeader(Response response) {
        String location = response.getHeader("Location");
        return Integer.valueOf(location.substring(location.lastIndexOf("/") + 1));
    }
}
