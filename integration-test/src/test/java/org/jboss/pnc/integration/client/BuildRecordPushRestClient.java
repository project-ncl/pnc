/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.integration.client;

import org.jboss.pnc.integration.client.util.RestResponse;
import org.jboss.pnc.rest.restmodel.BuildRecordPushRequestRest;

import java.util.Map;

public class BuildRecordPushRestClient extends AbstractRestClient<Map> {

    public static final String BUILD_RECORD_PUSH_REST_ENDPOINT = "/pnc-rest/rest/build-record-push/";

    public BuildRecordPushRestClient() {
        super(BUILD_RECORD_PUSH_REST_ENDPOINT, Map.class);
    }

    public RestResponse<Map> push(BuildRecordPushRequestRest buildRecordPushRequestRest) {
        return post(BUILD_RECORD_PUSH_REST_ENDPOINT, buildRecordPushRequestRest, true);
    }
}
