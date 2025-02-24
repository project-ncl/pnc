/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.rest.endpoints.internal;

import org.jboss.pnc.dto.tasks.RepositoryCreationResult;
import org.jboss.pnc.facade.providers.api.SCMRepositoryProvider;
import org.jboss.pnc.rest.endpoints.internal.api.BpmEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class BpmEndpointImpl implements BpmEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(BpmEndpointImpl.class);

    @Inject
    SCMRepositoryProvider scmRepositoryProvider;

    @Context
    private HttpServletRequest request;

    @Override
    public void repositoryCreationCompleted(RepositoryCreationResult repositoryCreationResult) {
        scmRepositoryProvider.repositoryCreationCompleted(repositoryCreationResult);
    }

    private String readContent(InputStream inputStream) throws IOException {

        try (InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(streamReader)) {

            StringBuilder result = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }

            return result.toString();
        }
    }
}
