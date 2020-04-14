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
package org.jboss.pnc.rest.api.parameters;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
public class BuildsFilterParameters {

    @Parameter(description = "Should return only latest build?")
    @QueryParam("latest")
    @DefaultValue("false")
    private boolean latest;

    @Parameter(description = "Should return only running builds?")
    @QueryParam("running")
    @DefaultValue("false")
    private boolean running;
}
