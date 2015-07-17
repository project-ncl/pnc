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

import java.util.Optional;

public class ClientResponse {

    private final Optional<Integer> id;
    private final int httpCode;
    private final AbstractRestClient parentCaller;

    ClientResponse(AbstractRestClient parentCaller, int httpCode, Integer id) {
        this.httpCode = httpCode;
        this.parentCaller = parentCaller;
        this.id = Optional.ofNullable(id);
    }

    ClientResponse(ProjectRestClient parentCaller, int httpCode) {
        this.httpCode = httpCode;
        this.parentCaller = parentCaller;
        this.id = Optional.empty();
    }

    public int getHttpCode() {
        return httpCode;
    }

    public Optional<Integer> getId() {
        return id;
    }
}
