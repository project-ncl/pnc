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
package org.jboss.pnc.integration.client;

import org.jboss.pnc.integration.client.util.RestResponse;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;

import java.util.List;

public class BuildRestClient extends AbstractRestClient<BuildRecordRest> {

    private static final String BUILD_RECORD_REST_ENDPOINT = "/pnc-rest/rest/builds/";
    private static final String AND_BY_BUILD_CONFIGURATION_NAME = "?andFindByBuildConfigurationName==";
    private static final String OR_BY_BUILD_CONFIGURATION_NAME = "?orFindByBuildConfigurationName==";

    public BuildRestClient() {
        super(BUILD_RECORD_REST_ENDPOINT, BuildRecordRest.class, false);
    }

    public RestResponse<List<BuildRecordRest>> findAndByBuildConfigurationName(
            boolean withValidation,
            int pageIndex,
            int pageSize,
            String rsql,
            String sort,
            String buildConfigurationName) {
        String url = BUILD_RECORD_REST_ENDPOINT + AND_BY_BUILD_CONFIGURATION_NAME + buildConfigurationName;
        return all(BuildRecordRest.class, url, withValidation, pageIndex, pageSize, rsql, sort);
    }

    public RestResponse<List<BuildRecordRest>> findOrByBuildConfigurationName(
            boolean withValidation,
            int pageIndex,
            int pageSize,
            String rsql,
            String sort,
            String buildConfigurationName) {
        String url = BUILD_RECORD_REST_ENDPOINT + OR_BY_BUILD_CONFIGURATION_NAME + buildConfigurationName;
        return all(BuildRecordRest.class, url, withValidation, pageIndex, pageSize, rsql, sort);
    }

}
