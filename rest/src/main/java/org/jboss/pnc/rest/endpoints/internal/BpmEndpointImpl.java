/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest.endpoints.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import org.jboss.pnc.dto.tasks.RepositoryCreationResult;
import org.jboss.pnc.facade.providers.api.SCMRepositoryProvider;
import org.jboss.pnc.rest.endpoints.internal.api.BpmEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
